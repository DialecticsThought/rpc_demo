package com.rpc.rpc_demo.registry;

import com.rpc.rpc_demo.config.RegistryConfig;
import com.rpc.rpc_demo.model.ServiceMetaData;

import java.util.List;

/**
 * @author jiahao.liu
 * @description
 * @date 2025/03/08 17:21
 */
public interface Registry {
    /**
     * 初始化
     * 一般是获取zk/etcd的客户端
     * @param registryConfig
     */
    void init(RegistryConfig registryConfig);

    /**
     * 注册服务 服务端
     *
     * @param serviceMetaData
     * @throws Exception
     */
    void register(ServiceMetaData serviceMetaData) throws Exception;

    /**
     * 取消注册服务 服务端
     *
     * @param serviceMetaData
     */
    void unRegister(ServiceMetaData serviceMetaData);

    /**
     * 服务发现  获取某服务的所有节点  客户端 消费端
     *
     * @param serviceIdentifier
     * @return
     */
    List<ServiceMetaData> serviceDiscovery(String serviceIdentifier);

    /**
     * 服务销毁
     */
    void destroy();

    /**
     * 心跳检测
     */
    void heartbeat();

    /**
     * 监听服务节点
     */
    void watch(String serviceNodeIdentifier);
}
