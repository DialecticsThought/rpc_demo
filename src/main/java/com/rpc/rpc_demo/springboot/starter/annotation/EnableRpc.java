package com.rpc.rpc_demo.springboot.starter.annotation;

import com.rpc.rpc_demo.constant.RpcConstant;
import com.rpc.rpc_demo.springboot.starter.bootstrap.RpcConsumerBootstrap;
import com.rpc.rpc_demo.springboot.starter.bootstrap.RpcInitBootStrap;
import com.rpc.rpc_demo.springboot.starter.bootstrap.RpcProviderBootstrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 19:03
 */
@Target({ElementType.FIELD})// 只能应用于类型(Type)级别,也就是类、接口或枚举
@Retention(RetentionPolicy.RUNTIME)// RUNTIME 表示该注解会在运行时被 JVM 读取和使用,可以被反射机制访问
// 这个注解用于导入其他配置类。在这里,它导入了三个引导类:
// RpcInitBootStrap: RPC 应用程序的初始化引导类。
// RpcProviderBootstrap: RPC 服务提供者的引导类。
// RpcConsumerBootstrap: RPC 服务消费者的引导类。
// 当 @EnableYunRpc 注解被应用到一个类上时,Spring 容器会自动注册这三个引导类
@Import({RpcInitBootStrap.class, RpcProviderBootstrap.class, RpcConsumerBootstrap.class})
public @interface EnableRpc {
    /**
     * 用于指定是否需要启动 RPC 服务端
     * 开发者可以通过设置这个属性的值来决定是否需要启动 RPC 服务端,例如在仅作为 RPC 客户端的场景下,可以将其设置为 false
     * @return
     */
    boolean needServer() default true;

}
