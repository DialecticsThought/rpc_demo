package com.rpc.rpc_demo.springboot.starter.bootstrap;

import com.rpc.rpc_demo.RpcContext;
import com.rpc.rpc_demo.config.RegistryConfig;
import com.rpc.rpc_demo.config.RpcConfig;
import com.rpc.rpc_demo.model.ServiceMetaData;
import com.rpc.rpc_demo.registry.Registry;
import com.rpc.rpc_demo.registry.RegistryFactory;
import com.rpc.rpc_demo.registry.LocalRegistry;
import com.rpc.rpc_demo.springboot.starter.annotation.RpcService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 21:20
 */
public class RpcProviderBootstrap implements BeanPostProcessor {
    /**
     * 提供者需要获取到所有包含@RpcService的注解的类，然后利用反射机制，获取到对应的注册信息，完成服务信息的注册
     * 让启动类实现BeanPostProcessor接口里的postProcessAfterInitialization方法，就可以在服务提供者Bean初始化之后，执行注册服务等操作了
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        // 获取注解
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        // 下面是获取注解的属性值
        if (rpcService != null) {
            // 从注解得到服务类
            Class<?> interfaceClass = rpcService.interfaceClass();
            // 默认值处理
            if (interfaceClass == void.class) {
                interfaceClass = beanClass.getInterfaces()[0];
            }
            // 哪一个服务 本质就是哪一个类的名字
            String serviceName = interfaceClass.getName();
            // 从注解得到服务版本
            String serviceVersion = rpcService.serviceVersion();

            // 注册服务
            // 本地注册
            LocalRegistry.register(serviceName, beanClass);

            // 全局配置
            final RpcConfig rpcConfig = RpcContext.getRpcConfig();
            // 注册到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaData serviceMetaData = new ServiceMetaData();
            // 设置服务名称
            serviceMetaData.setServiceName(serviceName);
            // 设置服务版本号
            serviceMetaData.setServiceVersion(serviceVersion);
            // 设置服务域名
            serviceMetaData.setServiceHost(rpcConfig.getServerHost());
            // 设置服务端口
            serviceMetaData.setServicePort(rpcConfig.getServerPort());
            try {
                // 注册服务
                registry.register(serviceMetaData);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
