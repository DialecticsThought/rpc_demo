package com.rpc.rpc_demo.config;


import com.rpc.rpc_demo.fault.retry.RetryStrategyIdentifier;
import com.rpc.rpc_demo.fault.tolerant.TolerantStrategyIdentifier;
import com.rpc.rpc_demo.loadbalancer.LoadBalancerIdentifier;
import com.rpc.rpc_demo.serializer.SerializerType;
import lombok.Data;

/**
 * @author jiahao.liu
 * @description
 * TODO 这个类的属性 可以写成 注解然后获取
 * @date 2025/03/08 17:11
 */
@Data
public class RpcConfig {

    /**
     * 名称
     */
    private String name = "rpc";

    /**
     * 版本号
     */
    private String version = "1.0";

    /**
     * 服务器主机
     */
    private String serverHost = "localhost";

    /**
     * 服务器端口
     */
    private int serverPort = 8080;

    /**
     * 模拟调用
     */
    private boolean mock = false;

    /**
     * 序列化器
     */
    private String serializer = SerializerType.JDK;

    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();

    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerIdentifier.ROUND_ROBIN;

    /**
     * 重试策略
     */
    private String retryStrategy = RetryStrategyIdentifier.NO;

    /**
     * 容错策略
     */
    private String tolerantStrategy = TolerantStrategyIdentifier.FAIL_FAST;
}

