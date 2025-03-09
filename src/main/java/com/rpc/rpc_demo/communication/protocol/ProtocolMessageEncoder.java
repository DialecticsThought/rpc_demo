package com.rpc.rpc_demo.communication.protocol;

import com.rpc.rpc_demo.serializer.Serializer;
import com.rpc.rpc_demo.serializer.factory.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 10:33
 */
public class ProtocolMessageEncoder {
    /**
     * 编码 TODO 代码和解码器对应
     *
     * @param message
     * @return
     */
    public static Buffer encode(ProtocolMessage<?> message) {
        if (message == null || message.getHeader() == null) {
            return Buffer.buffer();
        }
        ProtocolMessage.Header header = message.getHeader();
        // 依次向缓冲区写入字节 TODO 和Decode相对应
        Buffer buffer = Buffer.buffer();
        // 魔数
        buffer.appendByte(header.getMagic());
        // 请求体版本
        buffer.appendByte(header.getVersion());
        // 请求体序列化方式
        buffer.appendByte(header.getSerializer());
        // 请求类型
        buffer.appendByte(header.getType());
        // 请求状态
        buffer.appendByte(header.getStatus());
        // 请求id
        buffer.appendLong(header.getRequestId());
        // 获取序列化器
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum != null) {
            throw new RuntimeException("没有对应的序列化器");
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        try {
            byte[] bodyBytes = serializer.serialize(message.getBody());
            //写入 请求体 长度 和 本体
            buffer.appendInt(bodyBytes.length);
            buffer.appendBytes(bodyBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }
}
