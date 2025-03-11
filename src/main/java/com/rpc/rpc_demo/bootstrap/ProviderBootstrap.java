package com.rpc.rpc_demo.bootstrap;

import com.rpc.rpc_demo.RpcContext;
import com.rpc.rpc_demo.communication.server.VertxTcpServer;
import com.rpc.rpc_demo.config.RegistryConfig;
import com.rpc.rpc_demo.config.RpcConfig;
import com.rpc.rpc_demo.model.ServiceMetaData;
import com.rpc.rpc_demo.model.ServiceRegistrationMetaData;
import com.rpc.rpc_demo.registry.Registry;
import com.rpc.rpc_demo.registry.RegistryFactory;
import com.rpc.rpc_demo.registry.LocalRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 18:28
 */
@Slf4j
public class ProviderBootstrap {
    static RpcConfig rpcConfig;

    /**
     * 初始化
     * TODO 查看 coreProviderExample 类
     *
     * @param serviceRegisterInfoList
     */
    public static void init(List<ServiceRegistrationMetaData<?>> serviceRegisterInfoList) {
        // RPC上下文初始化
        RpcContext.init();
        // 全局配置
        rpcConfig = RpcContext.getRpcConfig();

        // 遍历传入的 ServiceRegisterInfo 列表,获取服务名和服务实现类
        for (ServiceRegistrationMetaData<?> serviceRegistrationMetaData : serviceRegisterInfoList) {
            // 得到服务名(提供服务的类的名字)
            String serviceName = serviceRegistrationMetaData.getServiceName();
            // 得到服务的实现类
            Class<?> serviceImplementClass = serviceRegistrationMetaData.getServiceImplementClass();

            // 将服务实现类注册到本地注册表 LocalRegistry 中,供 RPC 调用时使用
            LocalRegistry.register(serviceName, serviceImplementClass);

            // 注册到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            // 根据 RPC 配置中的注册中心信息,创建对应的注册中心实例 Registry 默认etcd
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());

            ServiceMetaData serviceMetaInfo = new ServiceMetaData();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try {
                // 真正注册 使用 registry.register() 方法将服务元信息(服务名、主机、端口等)注册到注册中心
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // 启动服务端
        // 创建 VertxTcpServer 实例,它是基于 Vert.x 框架实现的 TCP 服务端
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        // 调用 vertxTcpServer.doStart() 方法,并传入 RPC 配置中指定的服务端口,启动 TCP 服务端
        vertxTcpServer.doStart(RpcContext.getRpcConfig().getServerPort());

        //TODO 定时续约
    }
}
