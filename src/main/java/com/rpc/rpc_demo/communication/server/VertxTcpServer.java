package com.rpc.rpc_demo.communication.server;


import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description Vert.x创建一个TCP服务器实现
 * @Author veritas
 * @Data 2025/3/9 11:04
 */
@Slf4j
public class VertxTcpServer implements HttpServer {
    @Override
    public void doStart(int port) {
        // 创建一个vertx实例
        Vertx vertx = Vertx.vertx();
        // 创建一个TCP服务器
        NetServer tcpServer = vertx.createNetServer();
        // 处理连接请求
        // 主要涉及半包、粘包等问题的处理
        tcpServer.connectHandler();
        // 启动TCP服务器并监听指定端口
        tcpServer.listen(port, result -> {
            if(result.succeeded()) {
                log.info("TCP server is now listening on actual port:" +tcpServer.actualPort());
            }else {
                log.error("TCP server failed to listen on port:" +tcpServer.actualPort());
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpServer().doStart(8080);
    }
}
