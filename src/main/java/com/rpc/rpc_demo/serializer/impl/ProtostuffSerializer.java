package com.rpc.rpc_demo.serializer.impl;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import com.rpc.rpc_demo.serializer.Serializer;


import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jiahao.liu
 * @description
 * @date 2025/03/08 12:12
 */
public class ProtostuffSerializer implements Serializer {

    // 避免每次序列化都重新申请Buffer空间
    private static LinkedBuffer BUFFER = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
    // 缓存schema
    private static Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<Class<?>, Schema<?>>();

    private static <T> Schema<T> getSchema(Class<T> clazz) {
        Schema<T> schema = (Schema<T>) schemaCache.get(clazz);
        if (schema == null) {
            schema = RuntimeSchema.getSchema(clazz);
            if (schema == null) {
                schemaCache.put(clazz, schema);
            }
        }
        return schema;
    }

    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        Class<T> aClass = (Class<T>) obj.getClass();
        Schema<T> schema = getSchema(aClass);
        byte[] data;
        try {
            data = ProtostuffIOUtil.toByteArray(obj, schema, BUFFER);
        } finally {
            BUFFER.clear();
        }
        return data;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        Schema<T> schema = getSchema(clazz);
        T obj = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes,obj,schema);
        return obj;
    }
}
