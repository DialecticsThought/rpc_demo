package com.rpc.rpc_demo.springboot.starter.annotation;


import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.rpc.rpc_demo.constant.RpcConstant.DEFAULT_SERVICE_VERSION;

/**
 * @Description 当一个类被这个注解标注时, Spring 容器会自动扫描并注册该服务,同时也会提取服务接口类和版本号等元信息。
 * 这些信息可以在后续的服务发现和调用过程中使用。
 * @Author veritas
 * @Data 2025/3/9 19:04
 */
@Target({ElementType.TYPE})// 应用于类型(Type)级别,也就是类、接口或枚举
@Retention(RetentionPolicy.RUNTIME)// 保留策略是在运行时被 JVM 读取和使用
@Component// 被这个注解标注的类会被 Spring 容器自动扫描和注册
public @interface RpcService {
    /**
     * 服务接口类
     *
     * @return
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 服务版本
     *
     * @return
     */
    String serviceVersion() default DEFAULT_SERVICE_VERSION;
}
