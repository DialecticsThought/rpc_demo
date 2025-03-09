package com.rpc.rpc_demo.communication.server;

import com.rpc.rpc_demo.communication.protocol.*;
import com.rpc.rpc_demo.model.RpcRequest;
import com.rpc.rpc_demo.model.RpcResponse;
import com.rpc.rpc_demo.registry.impl.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.lang.reflect.Method;

/**
 * @Description 请求处理器（服务提供者用）
 * 请求处理器的主要作用是接受请求，通过反射调用对应的服务实现类
 * @Author veritas
 * @Data 2025/3/9 11:11
 */
public class TcpServerHandler implements Handler<NetSocket> {
    /**
     * 处理请求
     *
     * @param socket
     */
    @Override
    public void handle(NetSocket socket) {
        TcpBufferHandlerWrapper tcpBufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
            // 接受 请求， 解码
            ProtocolMessage<RpcRequest> protocolMessage;
            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (Exception e) {
                throw new RuntimeException("协议消息解码错误");
            }
            RpcRequest rpcRequest = protocolMessage.getBody();
            ProtocolMessage.Header header = protocolMessage.getHeader();

            // 处理请求
            // 构造响应结果对象
            RpcResponse rpcResponse = new RpcResponse();
            try {
                // 获取要调用的服务实现类，通过反射调用
                // 找到 服务提供者 所需要调用的方法的所属类
                Class<?> serviceImplementClass = LocalRegistry.get(rpcRequest.getServiceName());
                // 找到 服务提供者 所需要调用的方法
                Method method = serviceImplementClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                // 真正调用
                Object result = method.invoke(serviceImplementClass.newInstance(), rpcRequest.getArgs());
                // 封装响应
                rpcResponse.setData(result);
                // 数据类型 = 方法类型
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }
            // 设置消息头
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            header.setStatus((byte) ProtocolMessageStatusEnum.OK.getValue());
            // 发送响应
            ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
            try {
                // 编码
                Buffer encode = ProtocolMessageEncoder.encode(rpcResponseProtocolMessage);
                // 通过 socket.write() 方法写回给客户端
                socket.write(encode);
            } catch (Exception e) {
                throw new RuntimeException("协议消息编码错误");
            }
        });
        socket.handler(tcpBufferHandlerWrapper);
    }
}
