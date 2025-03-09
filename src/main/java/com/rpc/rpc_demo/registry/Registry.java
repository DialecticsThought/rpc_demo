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
     * 对服务提供者和消费者都是共用的，用于初始化和关闭 zk/etcd 客户端资源
     * 一般是获取zk/etcd的客户端
     *
     * @param registryConfig
     */
    void init(RegistryConfig registryConfig);

    /**
     * 服务提供者在启动时调用此方法将自己的服务信息注册到 etcd
     *
     * @param serviceMetaData
     * @throws Exception
     */
    void register(ServiceMetaData serviceMetaData) throws Exception;

    /**
     * 服务提供者在下线或退出时调用此方法注销自己的服务
     *
     * @param serviceMetaData
     */
    void unRegister(ServiceMetaData serviceMetaData);

    /**
     * 消费者在调用远程服务前通过该方法获取某个服务标识下所有可用的服务节点
     *
     * @param serviceIdentifier
     * @return
     */
    List<ServiceMetaData> serviceDiscovery(String serviceIdentifier);

    /**
     * 服务销毁
     * 对服务提供者和消费者都是共用的，用于初始化和关闭 zk/etcd 客户端资源
     */
    void destroy();

    /**
     * 服务提供者定时调用续约操作
     */
    void heartbeat();

    /**
     * 消费者通过此方法对指定的服务节点进行监听，以便在服务变更（例如服务下线或更新）时及时清除本地缓存，获取最新的服务信息。
     */
    void watch(String serviceNodeIdentifier);
}
