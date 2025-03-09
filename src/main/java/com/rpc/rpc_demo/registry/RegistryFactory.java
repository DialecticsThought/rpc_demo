package com.rpc.rpc_demo.registry;

import com.rpc.rpc_demo.registry.impl.EtcdRegistry;
import com.rpc.rpc_demo.spi.SPILoader;

/**
 * @Description 注册中心工厂
 * @Author veritas
 * @Data 2025/3/9 11:31
 */
public class RegistryFactory {
    static {
        // 本质是 META-INF 有一个名字Registry.class为全类名的文件
        // 里面每一个是key=value key是标识符，value是注册中心的实现的全类名
        SPILoader.load(Registry.class);
    }

    /**
     * 默认注册中心
     */
    private static final Registry DEFAULT_REGISTRY = new EtcdRegistry();

    /**
     * 获取实例
     */
    public static Registry getInstance(String key) {
        return SPILoader.getInstance(Registry.class, key);
    }
}
