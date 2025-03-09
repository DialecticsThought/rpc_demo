package com.rpc.rpc_demo.springboot.starter.annotation;

import com.rpc.rpc_demo.constant.RpcConstant;
import com.rpc.rpc_demo.fault.retry.RetryStrategyIdentifier;
import com.rpc.rpc_demo.fault.tolerant.TolerantStrategyIdentifier;
import com.rpc.rpc_demo.loadbalancer.LoadBalancerIdentifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 19:04
 */
@Target({ElementType.FIELD})// 只能被应用在字段(Field)级别
@Retention(RetentionPolicy.RUNTIME)// 保留策略是在运行时被 JVM 读取和使用
public @interface RpcReference {
    /**
     * 服务接口类 一个服务对应一个类
     * @return
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 服务版本
     * @return
     */
    String serviceVersion() default RpcConstant.DEFAULT_SERVICE_VERSION;

    /**
     * 负载均衡策略
     * @return
     */
    String loadBalancer() default LoadBalancerIdentifier.ROUND_ROBIN;

    /**
     * 重试策略
     * @return
     */
    String retryStrategy() default RetryStrategyIdentifier.NO;

    /**
     * 容错策略
     * @return
     */
    String tolerantStrategy() default TolerantStrategyIdentifier.FAIL_FAST;

    /**
     * 是否mock
     * @return
     */
    boolean mock() default false;
}
