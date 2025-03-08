package com.rpc.rpc_demo.serializer.factory;

import com.rpc.rpc_demo.serializer.Serializer;
import com.rpc.rpc_demo.spi.SPILoader;

/**
 * @author jiahao.liu
 * @description
 * @date 2025/03/08 17:05
 */
public class SerializerFactory {
    static {
        SPILoader.load(Serializer.class);
    }
    /**
     * 获取序列化器
     */
    public static Serializer getInstance(String key) {
        return SPILoader.getInstance(Serializer.class, key);
    }
}
