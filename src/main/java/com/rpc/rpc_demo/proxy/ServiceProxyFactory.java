package com.rpc.rpc_demo.proxy;

import com.rpc.rpc_demo.RpcContext;

import java.lang.reflect.Proxy;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 21:29
 */
public class ServiceProxyFactory {
    /**
     * 根据服务类 获取Mock代理对象
     *
     * @param serviceClass
     * @param <T>
     * @return
     */
    public static <T> T getMockProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new MockServiceProxy()
        );
    }

    /**
     * 根据服务类获取代理对象
     * 该方法首先检查 RPC 配置是否开启了 Mock 模式。
     * 如果开启了 Mock 模式,则调用 getMockProxy() 方法生成一个 Mock 代理对象。
     * 如果未开启 Mock 模式,则使用 Java 动态代理创建一个 ServiceProxy 对象作为代理。
     *
     * @param serviceClass
     * @param <T>
     * @return
     */
    public static <T> T getProxy(Class<T> serviceClass) {
        if (RpcContext.getRpcConfig().isMock()) {
            return getMockProxy(serviceClass);
        }
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy()
        );
    }
}
