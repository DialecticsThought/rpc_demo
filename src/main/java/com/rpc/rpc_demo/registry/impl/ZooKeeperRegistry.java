package com.rpc.rpc_demo.registry.impl;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.rpc.rpc_demo.config.RegistryConfig;
import com.rpc.rpc_demo.model.ServiceMetaData;
import com.rpc.rpc_demo.registry.RegisteredServiceCache;
import com.rpc.rpc_demo.registry.Registry;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author jiahao.liu
 * @description
 * @date 2025/03/08 19:24
 */
@Slf4j
public class ZooKeeperRegistry  implements Registry {
    /**
     * 根节点
     */
    private static final String ZK_ROOT_PATH = "/rpc";

    private CuratorFramework client;

    private ServiceDiscovery<ServiceMetaData> serviceDiscovery;

    /**
     * 本地注册节点 key 集合 用于维护续期
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
    @Override
    public void init(RegistryConfig registryConfig) {

    }

    @Override
    public void register(ServiceMetaData serviceMetaData) throws Exception {

    }

    @Override
    public void unRegister(ServiceMetaData serviceMetaData) {

    }

    @Override
    public List<ServiceMetaData> serviceDiscovery(String serviceIdentifier) {
        return List.of();
    }

    @Override
    public void destroy() {

    }

    @Override
    public void heartbeat() {

    }

    @Override
    public void watch(String serviceNodeIdentifier) {

    }
}
