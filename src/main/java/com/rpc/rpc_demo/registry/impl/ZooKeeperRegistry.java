package com.rpc.rpc_demo.registry.impl;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.rpc.rpc_demo.config.RegistryConfig;
import com.rpc.rpc_demo.model.ServiceMetaData;
import com.rpc.rpc_demo.registry.RegisteredServiceCache;
import com.rpc.rpc_demo.registry.Registry;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author jiahao.liu
 * @description
 * @date 2025/03/08 19:24
 */
@Slf4j
public class ZooKeeperRegistry implements Registry {
    /**
     * 根节点，所有注册的服务信息都在此节点下维护
     */
    private static final String ZK_ROOT_PATH = "/rpc";
    /**
     * CuratorFramework 客户端，用于与 ZK 服务器交互
     */
    private CuratorFramework client;
    /**
     * ServiceDiscovery 对象，用于在 ZK 上注册、发现服务
     */
    private ServiceDiscovery<ServiceMetaData> serviceDiscovery;
    /**
     * 本地注册节点 key 集合（用于维护续期或下线操作）
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();
    /**
     * 注册中心缓存，用于缓存查询到的服务列表
     */
    private final RegisteredServiceCache registryServiceCache = new RegisteredServiceCache();

    /**
     * 监听的key集合，避免重复监听
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    /**
     * 初始化注册中心，创建 CuratorFramework 和 ServiceDiscovery 实例并启动
     *
     * @param registryConfig 注册中心配置对象（包含ZK地址、超时时间等）
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        // 1. 构建 CuratorFramework 客户端，连接到 ZK
        client = CuratorFrameworkFactory
                .builder()
                .connectString(registryConfig.getAddress()) // 设置ZK地址
                .retryPolicy(new ExponentialBackoffRetry(Math.toIntExact(registryConfig.getTimeout()), 3)) // 重试策略
                .build();

        // 2. 使用 CuratorFramework 客户端构建 ServiceDiscovery 实例
        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMetaData.class)
                .client(client)
                .basePath(ZK_ROOT_PATH) // 指定在ZK中的根路径
                .serializer(new JsonInstanceSerializer<>(ServiceMetaData.class)) // 服务信息的序列化方式
                .build();

        try {
            // 3. 启动客户端和 ServiceDiscovery
            client.start();
            serviceDiscovery.start();
        } catch (Exception e) {
            // 启动失败时抛出异常
            throw new RuntimeException(e);
        }
    }

    /**
     * 注册服务，将服务信息封装为 ServiceInstance 并注册到ZK中
     *
     * @param ServiceMetaData 服务节点信息，包含主机、端口、服务标识等
     * @throws Exception 如果注册失败则抛出异常
     */
    @Override
    public void register(ServiceMetaData ServiceMetaData) throws Exception {
        // 1. 将服务信息封装为 ServiceInstance
        ServiceInstance<ServiceMetaData> instance = buildServiceInstance(ServiceMetaData);

        // 2. 调用 ServiceDiscovery 的 registerService 方法，将实例注册到 ZK
        serviceDiscovery.registerService(instance);

        // 3. 构造服务节点 key 并加入本地缓存
        String registerKey = ZK_ROOT_PATH + "/" + ServiceMetaData.getServiceNodeIdentifier();
        localRegisterNodeKeySet.add(registerKey);
    }

    /**
     * 注销服务，从 ZK 和本地缓存中移除对应节点
     *
     * @param ServiceMetaData 服务节点信息
     */
    @Override
    public void unRegister(ServiceMetaData ServiceMetaData) {
        try {
            // 1. 构造 ServiceInstance，并调用 unregisterService 进行注销
            serviceDiscovery.unregisterService(buildServiceInstance(ServiceMetaData));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 2. 从本地注册节点集合中移除
        String registerKey = ZK_ROOT_PATH + "/" + ServiceMetaData.getServiceNodeIdentifier();
        localRegisterNodeKeySet.remove(registerKey);
    }

    /**
     * 服务发现，根据服务key从ZK获取所有服务节点
     *
     * @param serviceIdentifier 服务标识，例如 "serviceName:serviceVersion"
     * @return 服务节点列表
     */
    @Override
    public List<ServiceMetaData> serviceDiscovery(String serviceIdentifier) {
        // 1. 先从本地缓存中获取
        List<ServiceMetaData> cachedServiceMetaDataList = registryServiceCache.readCache(serviceIdentifier);
        if (cachedServiceMetaDataList != null) {
            return cachedServiceMetaDataList;
        }

        try {
            // 2. 如果缓存为空，则调用 serviceDiscovery.queryForInstances 查询ZK
            Collection<ServiceInstance<ServiceMetaData>> serviceInstanceList = serviceDiscovery.queryForInstances(serviceIdentifier);

            // 3. 提取出每个 ServiceInstance 中的 payload（即 ServiceMetaData），组成列表
            List<ServiceMetaData> ServiceMetaDataList = serviceInstanceList.stream()
                    .map(ServiceInstance::getPayload)
                    .collect(Collectors.toList());

            // 4. 写入本地缓存
            registryServiceCache.writeCache(serviceIdentifier, ServiceMetaDataList);
            return ServiceMetaDataList;
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败", e);
        }
    }


    /**
     * 监听服务节点（通常在消费端使用），当节点发生变化时清除缓存
     *
     * @param serviceNodeKey 服务节点 key
     */
    @Override
    public void watch(String serviceNodeKey) {
        // 1. 构造要监听的全路径
        String watchKey = ZK_ROOT_PATH + "/" + serviceNodeKey;

        // 2. 如果尚未监听该 key，则添加到 watchingKeySet 并开始监听
        boolean newWatch = watchingKeySet.add(watchKey);
        if (newWatch) {
            // 3. 构建 CuratorCache，用于监听 ZK 节点的变化
            CuratorCache curatorCache = CuratorCache.build(client, watchKey);
            // 4. 启动 CuratorCache
            curatorCache.start();
            // 5. 添加监听器，监控节点的删除和更新事件，一旦发生变动就清除本地缓存
            curatorCache.listenable().addListener(
                    CuratorCacheListener
                            .builder()
                            .forDeletes(childData -> registryServiceCache.clearCache())
                            .forChanges(((oldNode, node) -> registryServiceCache.clearCache()))
                            .build()
            );
        }
    }

    /**
     * 销毁注册中心，主要是将当前节点下线并释放 ZK 相关资源
     */
    @Override
    public void destroy() {
        log.info("当前节点下线");
        // 1. 下线本地缓存的节点（如果是临时节点，客户端断开时也会被删除）
        for (String key : localRegisterNodeKeySet) {
            try {
                client.delete().guaranteed().forPath(key);
            } catch (Exception e) {
                throw new RuntimeException(key + "节点下线失败");
            }
        }

        // 2. 关闭 CuratorFramework 客户端
        if (client != null) {
            client.close();
        }
    }

    /**
     * 心跳机制
     * ZK 的 Curator 临时节点本身就会在客户端断开时删除，不需要额外心跳
     */
    @Override
    public void heartbeat() {
        // 由于使用了临时节点（Ephemeral），ZK会在客户端断开后自动删除节点
        // 因此不需要手动实现心跳机制
    }

    /**
     * 构造 ServiceInstance，用于注册和注销操作
     *
     * @param ServiceMetaData 服务元信息
     * @return 封装好的 ServiceInstance 对象
     */
    private ServiceInstance<ServiceMetaData> buildServiceInstance(ServiceMetaData ServiceMetaData) {
        // 将 serviceAddress 设置为 "host:port"
        String serviceAddress = ServiceMetaData.getServiceHost() + ":" + ServiceMetaData.getServicePort();
        try {
            // 构造 ServiceInstance 对象，包括 id、name、payload 等信息
            return ServiceInstance
                    .<ServiceMetaData>builder()
                    .id(serviceAddress)                       // 唯一标识
                    .name(ServiceMetaData.getServiceIdentifier())    // 服务名称（或 serviceKey）
                    .address(serviceAddress)                  // 注册节点的主机:端口
                    .payload(ServiceMetaData)                 // 服务的具体元信息
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
