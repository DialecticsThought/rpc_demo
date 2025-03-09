package com.rpc.rpc_demo.springboot.starter.bootstrap;

import com.rpc.rpc_demo.RpcContext;
import com.rpc.rpc_demo.communication.server.VertxTcpServer;
import com.rpc.rpc_demo.config.RpcConfig;
import com.rpc.rpc_demo.springboot.starter.annotation.EnableRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 21:18
 */
@Slf4j
public class RpcInitBootStrap implements ImportBeanDefinitionRegistrar {

    /**
     *在Spring框架初始化的时候，能够获取@EnableRpc注解，并且初始化RPC框架
     *
     * @param importingClassMetadata
     * @param registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获取EnableRpc 注解的属性值
        boolean needServer = (boolean) importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName()).get("needServer");

        // Rpc框架初始化（配置和注册中心）
        RpcContext.init();

        final RpcConfig rpcConfig = RpcContext.getRpcConfig();

        // 启动服务器
        if (needServer) {
            VertxTcpServer vertxTcpServer = new VertxTcpServer();
            vertxTcpServer.doStart(rpcConfig.getServerPort());
        } else {
            log.info("Rpc server is not started");
        }

    }
}
