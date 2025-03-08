package com.rpc.rpc_demo.registry.impl;

import com.rpc.rpc_demo.config.RegistryConfig;
import com.rpc.rpc_demo.model.ServiceMetaData;
import com.rpc.rpc_demo.registry.Registry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jiahao.liu
 * @description
 * @date 2025/03/08 17:49
 */
public class LocalRegistry implements Registry {

    /**
     * 注册信息存储
     */
    private  Map<String, Class<?>> map;

    public LocalRegistry() {
        init(null);
    }

    /**
     * 本地内存 不需要registryConfig
     * @param registryConfig
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        map = new ConcurrentHashMap<>();
    }

    @Override
    public void register(ServiceMetaData serviceMetaInfo) throws Exception {

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
