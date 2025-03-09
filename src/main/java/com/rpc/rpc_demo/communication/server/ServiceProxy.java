package com.rpc.rpc_demo.communication.server;

import cn.hutool.core.collection.CollUtil;
import com.rpc.rpc_demo.RpcContext;
import com.rpc.rpc_demo.config.RpcConfig;
import com.rpc.rpc_demo.constant.RpcConstant;
import com.rpc.rpc_demo.constant.TolerantStrategyConstant;
import com.rpc.rpc_demo.fault.retry.RetryStrategy;
import com.rpc.rpc_demo.fault.retry.RetryStrategyFactory;
import com.rpc.rpc_demo.fault.tolerant.TolerantStrategy;
import com.rpc.rpc_demo.fault.tolerant.TolerantStrategyFactory;
import com.rpc.rpc_demo.loadbalancer.LoadBalancer;
import com.rpc.rpc_demo.loadbalancer.LoadBalancerFactory;
import com.rpc.rpc_demo.model.RpcRequest;
import com.rpc.rpc_demo.model.RpcResponse;
import com.rpc.rpc_demo.model.ServiceMetaData;
import com.rpc.rpc_demo.registry.Registry;
import com.rpc.rpc_demo.registry.RegistryFactory;
import com.rpc.rpc_demo.serializer.Serializer;
import com.rpc.rpc_demo.serializer.factory.SerializerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description 请求发送（服务消费者）
 * @Author veritas
 * @Data 2025/3/9 18:06
 */
public class ServiceProxy  implements InvocationHandler {
    // 指定序列化器
    final Serializer serializer = SerializerFactory.getInstance(RpcContext.getRpcConfig().getSerializer());
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 1.构造请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        // 2.从注册中心获取 服务提供者的请求地址
        // 从上下文拿到核心配置
        RpcConfig rpcConfig = RpcContext.getRpcConfig();
        // 获取注册中心
        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
        ServiceMetaData serviceMetaData = new ServiceMetaData();
        // 构造请求
        // 这里服务名就是方法名字
        String serviceName = method.getDeclaringClass().getName();
        serviceMetaData.setServiceName(serviceName);
        serviceMetaData.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        // 找到 服务实例 列表
        List<ServiceMetaData> serviceMetaDataList = registry.serviceDiscovery(serviceMetaData.getServiceIdentifier());
        if (CollUtil.isEmpty(serviceMetaDataList)) {
            throw new RuntimeException("暂无可用服务提供者");
        }
        // 3.负载均衡
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", rpcRequest.getMethodName());
        ServiceMetaData metaData = loadBalancer.select(requestParams, serviceMetaDataList);

        // 发送TCP请求 (使用重试策略)
        RpcResponse response ;
        try {
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
            response = retryStrategy.doRetry(() -> {
                return VertxTcpClient.doRequest(rpcRequest, metaData);
            });
        } catch (Exception e) {
            TolerantStrategy strategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
            // 构造上下文
            Map<String, Object> context = new HashMap<>();
            context.put(TolerantStrategyConstant.SERVICE_LIST, serviceMetaDataList);
            context.put(TolerantStrategyConstant.CURRENT_SERVICE, metaData);
            context.put(TolerantStrategyConstant.RPC_REQUEST, rpcRequest);
            response = strategy.doTolerant(context, e);
        }
        return response.getData();
    }
}
