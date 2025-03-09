package com.rpc.rpc_demo.communication.protocol;

import com.rpc.rpc_demo.model.RpcRequest;
import com.rpc.rpc_demo.model.RpcResponse;
import com.rpc.rpc_demo.serializer.Serializer;
import com.rpc.rpc_demo.serializer.factory.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

/**
 * @Description 协议消息解码器
 * @Author veritas
 * @Data 2025/3/9 10:41
 */
public class ProtocolMessageDecoder {
    /**
     * 解码 TODO 代码和编码器对应
     * * @param buffer
     *
     * @return
     */
    public static ProtocolMessage decode(Buffer buffer) {
        // 从指定位置 读取 buffer
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        byte magic = buffer.getByte(0);

        if (magic != ProtocolConstant.PROTOCOL_MAGIC) {
            throw new RuntimeException("invalid magic!");
        }
        // 得到魔数
        header.setMagic(magic);
        // 得到版本号
        header.setVersion(buffer.getByte(1));
        // 得到序列化器
        header.setSerializer(buffer.getByte(2));
        // 得到消息类型
        header.setType(buffer.getByte(3));
        // 得到消息状态
        header.setStatus(buffer.getByte(4));
        // 得到请求id
        header.setRequestId(buffer.getLong(5));
        header.setBodyLength(buffer.getInt(13));
        // 13 + 4(因为int) = 17
        // 解决粘包问题，只读取指定长度的数据
        byte[] bodyBytes = buffer.getBytes(17, 17 + header.getBodyLength());
        // 解析消息体
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("序列化消息的协议不存在");
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        ProtocolMessageTypeEnum messageTypeEnum = ProtocolMessageTypeEnum.getEnum(header.getType());
        if (messageTypeEnum == null) {
            throw new RuntimeException("序列化消息的类型不存在");
        }
        try {
            switch (messageTypeEnum) {
                case REQUEST:
                    RpcRequest request = null;
                    request = serializer.deserialize(bodyBytes, RpcRequest.class);
                    return new ProtocolMessage<>(header, request);
                case RESPONSE:
                    RpcResponse response = serializer.deserialize(bodyBytes, RpcResponse.class);
                    return new ProtocolMessage<>(header, response);
                case HEAT_BEAT:
                case OTHER:
                default:
                    throw new RuntimeException("不支持的消息类型");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
