package com.rpc.rpc_demo.communication.server;

import cn.hutool.core.util.IdUtil;
import com.rpc.rpc_demo.RpcContext;
import com.rpc.rpc_demo.communication.protocol.*;
import com.rpc.rpc_demo.model.RpcRequest;
import com.rpc.rpc_demo.model.RpcResponse;
import com.rpc.rpc_demo.model.ServiceMetaData;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 11:10
 */
@Slf4j
public class VertxTcpClient {
    /**
     * 发送RPC请求，并等待返回响应
     *
     * @param rpcRequest
     * @param metaData
     * @return
     * @throws Exception
     */
    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaData metaData) throws Exception {
        // 创建Vert.x实例，用于异步事件处理和网络操作
        Vertx vertx = Vertx.vertx();
        // 创建TCP客户端，用于建立连接
        NetClient netClient = vertx.createNetClient();
        // 创建CompletableFuture，用于异步接收RPC响应
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
        // 尝试连接到目标服务，metaData中包含服务的主机和端口信息
        netClient.connect(metaData.getServicePort(), metaData.getServiceHost(), result -> {
            // 如果连接失败，则输出错误信息
            if (!result.succeeded()) {
                log.error("Failed to connect to TCP server");
                return; // 连接失败时直接返回，不继续执行后续代码
            }
            // 连接成功，打印成功提示
            log.info("Connected to TCP server");
            // 获取建立的NetSocket实例，用于数据收发
            NetSocket socket = result.result();

            // 构造协议消息对象，该对象封装了请求数据和消息头信息
            ProtocolMessage<Object> protocolMessage = new ProtocolMessage<>();
            ProtocolMessage.Header header = new ProtocolMessage.Header();
            // 设置协议魔数，用于标识协议格式
            header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
            // 设置协议版本
            header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
            // 设置序列化方式，这里通过配置获取对应的序列化器key
            header.setSerializer((byte) ProtocolMessageSerializerEnum
                    .getEnumByValue(RpcContext.getRpcConfig().getSerializer())
                    .getKey());
            // 设置消息类型，这里表示为请求消息
            header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
            // 生成并设置请求ID，用于唯一标识一次请求
            header.setRequestId(IdUtil.getSnowflakeNextId());
            // 将构造好的消息头设置到协议消息中
            protocolMessage.setHeader(header);
            // 将实际的RPC请求数据设置到协议消息中
            protocolMessage.setBody(rpcRequest);

            // 尝试对协议消息进行编码，并写入TCP连接中发送出去
            try {
                // 编码后的Buffer数据，准备发送到服务端
                Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
                // 将编码后的数据写入到socket
                socket.write(encodeBuffer);
            } catch (Exception e) {
                // 如果编码或发送过程中出现异常，则抛出运行时异常
                throw new RuntimeException(e);
            }

            // 创建一个TCP缓冲区处理包装器，用于处理服务端返回的响应数据
            // 给包装类传入一个匿名实现类，这个匿名实现类的方法 是在当消息体数据接收完成后 执行的
            TcpBufferHandlerWrapper tcpBufferHandlerWrapper = new TcpBufferHandlerWrapper(new Handler<Buffer>() {
                @Override
                public void handle(Buffer buffer) {
                    try {
                        // 解码接收到的Buffer数据，得到协议消息对象（包含响应数据）
                        ProtocolMessage<RpcResponse> responseProtocolMessage =
                                (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                        // 将响应数据传递给CompletableFuture，完成异步调用
                        responseFuture.complete(responseProtocolMessage.getBody());
                    } catch (Exception e) {
                        // 如果解码过程中出现异常，则抛出运行时异常，提示协议消息编码错误
                        throw new RuntimeException("协议消息码错误");
                    }
                }
            });
            // 将上面的处理器设置到socket上，用于处理接收到的数据
            socket.handler(tcpBufferHandlerWrapper);
        });

        log.info("Waiting for response");
        // 定义响应消息
        RpcResponse rpcResponse = null;
        // ("Waiting for response");
        try {
            rpcResponse = responseFuture.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 打印提示信息，表示响应已接收到
        log.info("Received response");
        // 关闭TCP客户端连接
        netClient.close();
        // 返回接收到的RPC响应
        return rpcResponse;
    }

    // 启动TCP客户端连接，并发送多个请求示例
    public void start() {
        // 创建Vert.x实例
        Vertx vertx = Vertx.vertx();
        // 创建TCP客户端，并尝试连接到本地8082端口的TCP服务器
        vertx.createNetClient().connect(8082, "localhost", res -> {
            // 如果连接成功
            if (res.succeeded()) {
                System.out.println("Connected to Tcp Server!");
                // 获取连接成功后的socket实例
                NetSocket socket = res.result();
                // 发送1000个请求示例
                for (int i = 0; i < 1000; i++) {
                    // 创建一个Buffer用于存储数据
                    Buffer buffer = Buffer.buffer();
                    // 构造发送的字符串
                    String str = "hello,server!hello,server!hello,server!hello,server!";
                    // 预先写入一个整数(可能用于占位或协议约定)
                    buffer.appendInt(0);
                    // 写入字符串的字节长度
                    buffer.appendInt(str.getBytes().length);
                    // 打印发送数据的信息
                    System.out.println("Send data to server:" + str);
                    // 将字符串的字节数据追加到Buffer中
                    buffer.appendBytes(str.getBytes());
                    // 通过socket将Buffer中的数据发送到服务器
                    socket.write(buffer);
                }
                // 设置接收数据的处理器
                socket.handler(buffer -> {
                    // 每当接收到数据时，打印接收到的内容
                    System.out.println("Received data from server:" + buffer.toString());
                });
            } else {
                // 如果连接失败，输出失败信息及原因
                System.out.println("Failed to connect: " + res.cause().getMessage());
            }
        });
    }

    // 程序入口：启动客户端
    public static void main(String[] args) {
        new VertxTcpClient().start();
    }
}
