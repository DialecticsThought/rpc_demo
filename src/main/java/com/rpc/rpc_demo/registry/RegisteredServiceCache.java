package com.rpc.rpc_demo.registry;

import com.rpc.rpc_demo.model.ServiceMetaData;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jiahao.liu
 * @description
 * @date 2025/03/08 17:54
 */
public class RegisteredServiceCache {
    /**
     * 服务缓存
     * key = ServiceNodeIdentifier
     * 缓存多个服务的信息：Key 为服务标识，Value 为对应的服务列表
     */
    private Map<String, List<ServiceMetaData>> serviceCache = new ConcurrentHashMap<>();

    /**
     * 写缓存：保存指定服务标识的服务列表
     */
    public void writeCache(String serviceIdentifier, List<ServiceMetaData> newServiceCache) {
        serviceCache.put(serviceIdentifier, newServiceCache);
    }

    /**
     * 读缓存：获取指定服务标识的服务列表
     */
    public List<ServiceMetaData> readCache(String serviceIdentifier) {
        return serviceCache.get(serviceIdentifier);
    }

    /**
     * 清空指定服务标识的缓存
     */
    public void clearCache(String serviceIdentifier) {
        serviceCache.remove(serviceIdentifier);
    }

    /**
     * 清空所有缓存
     */
    public void clearCache() {
        serviceCache.clear();
    }
}
