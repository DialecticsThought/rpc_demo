package com.rpc.rpc_demo.registry.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.json.JSONUtil;
import com.rpc.rpc_demo.config.RegistryConfig;
import com.rpc.rpc_demo.model.ServiceMetaData;
import com.rpc.rpc_demo.registry.RegisteredServiceCache;
import com.rpc.rpc_demo.registry.Registry;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.log4j.Log4j2;
import cn.hutool.cron.task.Task;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.etcd.jetcd.watch.WatchEvent.EventType.DELETE;

/**
 * @author jiahao.liu
 * @description
 * @date 2025/03/08 17:51
 */
@Slf4j
public class EtcdRegistry implements Registry {

    /**
     * 根节点
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";
    /**
     * etcd 客户端（jetcd 的 Client），与 etcd 服务器建立连接
     */
    private Client client;
    /**
     * KV 客户端（jetcd 中的 KV），用于执行 put、get、delete 等键值操作
     */
    private KV kvClient;

    /**
     * 本地注册节点 key 集合，用于维护续约
     * 每一个元素形如： serviceName:serviceVersion/serviceHost:servicePort
     * 注意末尾有 "/"，例如： "testService:v1/127.0.0.1:8080"
     * 当注册成功后，将该 key 存入集合，用于后续的续约或注销
     * 主要由 服务提供者 使用
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();
    /**
     * 注册中心缓存，用于缓存 serviceDiscovery 查询到的服务信息
     * 主要由 消费者 使用，在调用 serviceDiscovery 方法时首先从缓存中获取服务列表；如果缓存没有，再从 etcd 查询后将结果写入缓存
     */
    private final RegisteredServiceCache registryServiceCache = new RegisteredServiceCache();
    /**
     * 监听的 key 集合，用于避免重复监听
     * 当我们对某个 serviceNodeIdentifier 设置 watch 后，将其加入本集合
     * 主要由 消费者 使用
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    /**
     * 初始化方法，本质上是获取 etcd 客户端，建立连接
     *
     * @param registryConfig 包含 etcd 的地址、超时时间等配置
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        // 1. 创建 etcd 客户端，指定 etcd 地址和连接超时
        client = Client.builder()
                .endpoints(registryConfig.getAddress())// etcd 服务器地址，可以是多个
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))// 连接超时时间
                .build();
        // 2. 通过 client 获取 KV 客户端，用于后续 put/get/delete 等操作
        kvClient = client.getKVClient();
        // 3. 启动心跳续约机制
        heartbeat();
    }

    /**
     * 注册服务，将 ServiceMetaData 写入 etcd
     * 并将该节点 key 缓存在 localRegisterNodeKeySet 中，用于续约和下线操作
     * 服务提供者在启动时调用此方法将自己的服务信息注册到 etcd
     *
     * @param serviceMetaData 服务节点信息
     * @throws Exception 可能抛出网络或序列化等异常
     */
    @Override
    public void register(ServiceMetaData serviceMetaData) throws Exception {
        // 1. 获取 LeaseClient，用于管理租约
        Lease leaseClient = client.getLeaseClient();
        // 2. 创建一个 30 秒的租约（租约到期后，如果不续约，该 key 会被 etcd 自动删除）
        long leaseId = leaseClient.grant(30).get().getID();
        // 3. 构造要注册的 key 和 value
        String identifierAboutToRegister = ETCD_ROOT_PATH + serviceMetaData.getServiceNodeIdentifier();
        // key 形如: /rpc/serviceName:serviceVersion/serviceHost:servicePort
        ByteSequence key = ByteSequence.from(identifierAboutToRegister, StandardCharsets.UTF_8);
        // value 是序列化后的 ServiceMetaData
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaData), StandardCharsets.UTF_8);
        // 4. 将键值对与租约绑定，即 putOption 绑定了该 leaseId
        // 租约过期后，etcd 会自动删除此 key
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        // 5. 向 etcd 写入 key-value，并绑定租约
        kvClient.put(key, value, putOption).get();
        // 6. 将该 key 加入本地集合，用于续约和下线
        localRegisterNodeKeySet.add(identifierAboutToRegister);
    }

    /**
     * 注销服务，从 etcd 和本地缓存中删除
     * 服务提供者在下线或退出时调用此方法注销自己的服务
     *
     * @param serviceMetaData 服务节点信息
     */
    @Override
    public void unRegister(ServiceMetaData serviceMetaData) {
        // 1. 构造要删除的 key
        String registerKey = ETCD_ROOT_PATH + serviceMetaData.getServiceNodeIdentifier();
        // 2. 调用 etcd 的 delete 操作
        kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8));
        // 3. 从本地集合中移除
        localRegisterNodeKeySet.remove(registerKey);
    }

    /**
     * 服务发现，根据 serviceIdentifier 查询所有匹配的节点
     * serviceIdentifier 形如: serviceName:serviceVersion
     * 消费者在调用远程服务前通过该方法获取某个服务标识下所有可用的服务节点
     *
     * @param serviceIdentifier 服务标识
     * @return 返回所有匹配的 ServiceMetaData
     */
    @Override
    public List<ServiceMetaData> serviceDiscovery(String serviceIdentifier) {
        // 1. 优先从本地缓存中读取
        List<ServiceMetaData> serviceMetaInfoList = registryServiceCache.readCache(serviceIdentifier);
        if (CollUtil.isNotEmpty(serviceMetaInfoList)) {
            return serviceMetaInfoList;
        }
        // 2. 缓存中没有，则到 etcd 查询
        //   构造查询前缀: /rpc/serviceName:serviceVersion/
        String searchPrefix = ETCD_ROOT_PATH + serviceIdentifier + "/";
        GetOption getOption = GetOption.builder().isPrefix(true).build();
        try {
            // 2.1 创建一个 30 秒的租约，用于该查询（可选逻辑，具体看需求）
            Lease leaseClient = client.getLeaseClient();
            long leaseId = leaseClient.grant(30).get().getID();
            PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
            // 2.2 执行 get 查询，拿到所有匹配前缀的 KeyValue
            List<KeyValue> kvs = kvClient.get(ByteSequence.from(searchPrefix, StandardCharsets.UTF_8), getOption).get().getKvs();
            // 2.3 将每个 KeyValue 的 value 解析成 ServiceMetaData
            //     并对每个 key 调用 watch 方法，监听后续变更
            List<ServiceMetaData> serviceMetaDataList = kvs.stream().map(keyValue -> {
                // keyValue.getValue() 是序列化后的 JSON
                String key = keyValue.getValue().toString(StandardCharsets.UTF_8);
                // watch(key) 使用的是 jsonString，而实际上这里要注意：keyValue.getKey() 才是节点路径
                // 代码中 watch(key) 是在 watch(...) 方法里针对 serviceNodeIdentifier 做监听
                watch(key);
                // 反序列化为 ServiceMetaData
                return JSONUtil.toBean(key, ServiceMetaData.class);
            }).collect(Collectors.toList());
            // 2.4 将查询结果写入本地缓存
            registryServiceCache.writeCache(serviceIdentifier, serviceMetaDataList);

            // 2.5 返回查询到的列表
            return serviceMetaDataList;
        } catch (Exception e) {
            throw new RuntimeException("服务发现失败", e);
        }
    }

    /**
     * 销毁操作，当前节点下线
     * 遍历 localRegisterNodeKeySet 删除所有注册的 key，并关闭客户端
     */
    @Override
    public void destroy() {
        log.info("当前节点下线");
        // 1. 下线所有已注册的服务
        for (String key : localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
                System.out.println("下线:" + key);
            } catch (Exception e) {
                throw new RuntimeException(key + "下线失败", e);
            }
        }

        // 2. 关闭 etcd 客户端
        if (client != null) {
            client.close();
        }
        if (kvClient != null) {
            kvClient.close();
        }
    }


    /**
     * 这里的心跳（heartbeat）实现是每隔10秒重新注册一次已注册的服务（续约）
     * 租约是30秒，不涉及和服务提供者通信，仅在 etcd 中进行续租操作
     * <p>
     * 服务提供者定时调用续约操作，确保自己的注册信息不会因租约过期而被 etcd 自动删除
     */
    @Override
    public void heartbeat() {
        // 使用 Hutool 的 CronUtil 来定时执行任务，表达式 "*/10 * * * * *" 表示每隔10秒执行一次
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                // 遍历本地保存的所有已注册 key
                for (String key : localRegisterNodeKeySet) {
                    try {
                        // 从 etcd 获取该 key 的信息，若为空，说明已过期
                        List<KeyValue> kvs = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8)).get().getKvs();
                        if (CollUtil.isEmpty(kvs)) {
                            // 如果租约已过期，则不再续约
                            continue;
                        }
                        // 如果 key 仍存在，则重新注册，相当于续约操作
                        KeyValue keyValue = kvs.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaData serviceMetaData = JSONUtil.toBean(value, ServiceMetaData.class);
                        log.info("续约：{}", serviceMetaData.getServiceNodeIdentifier());
                        // 重新执行 register，这会创建新的租约并写入 etcd
                        register(serviceMetaData);
                    } catch (Exception e) {
                        throw new RuntimeException(key + "续约失败", e);
                    }
                }
            }
        });
    }

    /**
     * watch 方法，用于监听某个 serviceNodeIdentifier 的变更
     * 当 etcd 上的该 key 被删除或修改时，清空本地缓存
     * 消费者通过此方法对指定的服务节点进行监听，以便在服务变更（例如服务下线或更新）时及时清除本地缓存，获取最新的服务信息。
     *
     * @param serviceNodeIdentifier 形如 "serviceName:serviceVersion/serviceHost:servicePort"
     */
    @Override
    public void watch(String serviceNodeIdentifier) {
        // 1. 获取 etcd 的 watch 客户端
        Watch watchClient = client.getWatchClient();

        // 2. 若尚未监听过该 key，则添加到 watchingKeySet 并开始监听
        boolean isWatched = watchingKeySet.add(serviceNodeIdentifier);
        if (isWatched) {
            // 3. 调用 watchClient.watch 方法，传入要监听的 key
            watchClient.watch(ByteSequence.from(serviceNodeIdentifier, StandardCharsets.UTF_8), (response) -> {
                // 4. 当该 key 发生事件时，遍历所有 WatchEvent
                for (WatchEvent event : response.getEvents()) {
                    // 5. 如果是 DELETE 事件，说明节点被删除，清除本地缓存
                    switch (event.getEventType()) {
                        case DELETE:
                            registryServiceCache.clearCache();
                            break;
                        case PUT:
                        default:
                            // PUT 表示新增或修改，同样可以根据需要处理，这里默认不做操作
                            break;
                    }
                }
            });
        }
    }
}
