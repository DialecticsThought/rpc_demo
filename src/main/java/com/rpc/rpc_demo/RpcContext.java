package com.rpc.rpc_demo;

import com.rpc.rpc_demo.config.RegistryConfig;
import com.rpc.rpc_demo.config.RpcConfig;
import com.rpc.rpc_demo.constant.RpcConstant;
import com.rpc.rpc_demo.registry.Registry;
import com.rpc.rpc_demo.registry.RegistryFactory;
import com.rpc.rpc_demo.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description 存放了项目全局用到的变量，双检锁实现单例
 * 管理 RPC 框架的入口和全局配置管理器，我们希望在里面可以集中管理这些配置选项，并且可以轻松获取这些选项，
 * eg:
 * 初始化Rpc配置信息
 * 通过单例模式来获取上述的Rpc配置信息
 * @Author veritas
 * @Data 2025/3/9 11:15
 */
@Slf4j
public class RpcContext {

    private static volatile RpcConfig rpcConfig;

    /**
     * 为无参init方法服务
     * @param newRpcConfig
     */
    public static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;
        log.info("rpc application init success,config:{}", rpcConfig);
        // 注册中心的初始化
        // 获取注册中心的核心配置
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        // 实例化
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig);
        log.info("registry init success,config:{}", registryConfig);
        // 创建并 注册Shutdown Hook ,JVM 退出时执行销毁
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }

    public static void init() {
        RpcConfig newRpcConfig;
        try {
            // 先去配置文件中拿
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            // 读取配置文件失败，使用默认配置
            log.error("load config error,use default config", e);
            newRpcConfig = new RpcConfig();
        }
        init(rpcConfig);
    }


    /**
     * 获取配置
     */
    public static RpcConfig getRpcConfig() {
        if (rpcConfig == null) {
            synchronized (RpcContext.class) {
                if (rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
