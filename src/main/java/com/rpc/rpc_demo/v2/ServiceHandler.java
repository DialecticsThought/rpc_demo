package com.rpc.rpc_demo.v2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author jiahao.liu
 * @description
 * @date 2025/03/08 16:27
 */
public class ServiceHandler implements InvocationHandler {
    // 指定序列化器
    //final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
