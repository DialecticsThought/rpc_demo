package com.rpc.rpc_demo.bootstrap;

import com.rpc.rpc_demo.RpcContext;

/**
 * @Description 服务消费者启动类
 * @Author veritas
 * @Data 2025/3/9 18:50
 */
public class ConsumerBootstrap {
    /**
     * 消费者启动只需要初始化配置
     */
    public static void init() {
        RpcContext.init();
    }
}
