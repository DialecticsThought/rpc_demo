package com.rpc.rpc_demo.serializer.impl;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.rpc.rpc_demo.model.RpcRequest;
import com.rpc.rpc_demo.model.RpcResponse;
import com.rpc.rpc_demo.serializer.Serializer;

import java.io.IOException;

/**
 * @author jiahao.liu
 * @description
 * @date 2025/03/08 11:25
 */
public class JSONSerializer implements Serializer {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(obj);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        T obj = OBJECT_MAPPER.readValue(bytes, clazz);
        if(obj instanceof RpcRequest) {
            return handleRequest((RpcRequest)obj, clazz);
        }
        if(obj instanceof RpcResponse) {
            return handleResponse((RpcResponse)obj, clazz);
        }
        return obj;
    }

    /**
     * 由于Object 的原始对象会被擦除，导致反序列化时 LinkedHashMap 无法转换为 原始对象，这里需要特殊处理
     *
     * @param rpcRequest
     * @param type
     * @param <T>
     * @return
     */
    public <T> T handleRequest(RpcRequest rpcRequest, Class<T> type) throws IOException {
        // 参数类型列表
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        // 参数列表
        Object[] args = rpcRequest.getArgs();

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> clazz = parameterTypes[i];
            // 如果类型 与 参数的真正的类型不同
            if(!clazz.isAssignableFrom(args[i].getClass())) {
                byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(args[i]);

                args[i] = OBJECT_MAPPER.readValue(bytes, clazz);
            }
        }
        return type.cast(rpcRequest);
    }

    /**
     * 由于Object 的原始对象会被擦除，导致反序列化时 LinkedHashMap 无法转换为 原始对象，这里需要特殊处理
     *
     * @param rpcResponse
     * @param type
     * @param <T>
     * @return
     * @throws IOException
     */
    public <T> T handleResponse(RpcResponse rpcResponse, Class<T> type) throws IOException {
        byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(rpcResponse.getData());
        rpcResponse.setData(OBJECT_MAPPER.readValue(bytes,rpcResponse.getDataType()));

        return type.cast(rpcResponse);
    }
}
