package com.rpc.rpc_demo.communication.server;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 11:05
 */
public interface HttpServer {
    /**
     * 启动服务器
     * @param port 端口
     */
    void doStart(int port);
}
