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

    private Client client;

    private KV kvClient;

    /**
     * 本地注册节点 key 集合 用于维护续期
     * 每一个元素：
     * eg:serviceName:serviceVersion/serviceHost:servicePort,记住"/"这个
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();
    /**
     * 注册中心缓存
     */
    private final RegisteredServiceCache registryServiceCache = new RegisteredServiceCache();
    /**
     * 监听的key集合
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    /**
     * 初始化本质就是获取 etcd的客户端
     *
     * @param registryConfig
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();

        heartbeat();
    }

    @Override
    public void register(ServiceMetaData serviceMetaData) throws Exception {
        // 创建Lease 和KV客户端
        Lease leaseClient = client.getLeaseClient();
        // 创建租约 30s
        long leaseId = leaseClient.grant(30).get().getID();
        // 设置要存储的键值对
        String identifierAboutToRegister = ETCD_ROOT_PATH + serviceMetaData.getServiceNodeIdentifier();

        ByteSequence key = ByteSequence.from(identifierAboutToRegister, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaData), StandardCharsets.UTF_8);
        // 将键值对与租约绑定 并设置过期时间
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        // etcd 设置
        kvClient.put(key, value, putOption).get();
        // 本地设置
        localRegisterNodeKeySet.add(identifierAboutToRegister);
    }

    @Override
    public void unRegister(ServiceMetaData serviceMetaData) {
        String registerKey = ETCD_ROOT_PATH + serviceMetaData.getServiceNodeIdentifier();
        // etcd 设置
        kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8));
        // 本地设置
        localRegisterNodeKeySet.remove(registerKey);
    }

    @Override
    public List<ServiceMetaData> serviceDiscovery(String serviceIdentifier) {
        // 先从本地缓存拿
        List<ServiceMetaData> serviceMetaInfoList = registryServiceCache.readCache(serviceIdentifier);
        if (CollUtil.isNotEmpty(serviceMetaInfoList)) {
            return serviceMetaInfoList;
        }
        // 没有 去etcd
        String searchPrefix = ETCD_ROOT_PATH + serviceIdentifier + "/";
        GetOption getOption = GetOption.builder().isPrefix(true).build();
        try {
            Lease leaseClient = client.getLeaseClient();
            // 创建租约 30s
            long leaseId = leaseClient.grant(30).get().getID();
            PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
            List<KeyValue> kvs = kvClient.get(ByteSequence.from(searchPrefix, StandardCharsets.UTF_8), getOption).get().getKvs();
            //解析服务
            List<ServiceMetaData> serviceMetaDataList = kvs.stream().map(keyValue -> {
                String key = keyValue.getValue().toString(StandardCharsets.UTF_8);
                // 监听key的变化
                watch(key);
                return JSONUtil.toBean(key, ServiceMetaData.class);
            }).collect(Collectors.toList());
            // 写入缓存
            registryServiceCache.writeCache(serviceIdentifier, serviceMetaDataList);
            return serviceMetaDataList;
        } catch (Exception e) {
            throw new RuntimeException("服务发现失败", e);
        }
    }

    @Override
    public void destroy() {
        log.info("当前节点下线");
        //下线所有的服务
        for (String key : localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
                System.out.println("下线:" + key);
            } catch (Exception e) {
                throw new RuntimeException(key + "下线失败", e);
            }
        }
        // 关闭客户端
        if (client != null) {
            client.close();
        }
        if (kvClient != null) {
            kvClient.close();
        }
    }

    /**
     * 这里的心跳 是没10s刷新租约 ，租约是30s，不涉及和服务提供者通信
     */
    @Override
    public void heartbeat() {
        // 10s续约一次
        CronUtil.schedule("*/10 * * * * *",new Task(){
            @Override
            public void execute() {
                for(String key : localRegisterNodeKeySet){
                    try {
                        List<KeyValue> kvs = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8)).get().getKvs();
                        // 节点已经过期了，需要重启节点才能重新注册
                        if (CollUtil.isEmpty(kvs)) {
                            continue;
                        }
                        // 节点没有过期，重新 注册 相当于续约
                        KeyValue keyValue = kvs.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaData serviceMetaData = JSONUtil.toBean(value, ServiceMetaData.class);
                        log.info("续约：{}",serviceMetaData.getServiceNodeIdentifier());
                        register(serviceMetaData);
                    }catch (Exception e){
                        throw new RuntimeException(key + "续约失败", e);
                    }
                }
            }
        });
    }

    @Override
    public void watch(String serviceNodeIdentifier) {
        Watch watchClient = client.getWatchClient();
        // 之间未被监听，添加监听
        boolean isWatched = watchingKeySet.add(serviceNodeIdentifier);
        if(isWatched){
            watchClient.watch(ByteSequence.from(serviceNodeIdentifier,StandardCharsets.UTF_8),(response)->{
               for(WatchEvent event:response.getEvents()){
                   switch (event.getEventType()) {
                       case DELETE:
                           registryServiceCache.clearCache();
                           break;
                       case PUT:
                       default:
                           break;
                   }
               }
            });
        }
    }
}
