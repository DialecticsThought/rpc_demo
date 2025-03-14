package com.rpc.rpc_demo.communication.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jiahao.liu
 * @description
 * @date 2025/03/08 19:35
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolMessage<T> {
    /**
     * 消息头
     */
    private Header header;

    /**
     * 消息体 （请求或响应对象）
     */
    private T body;

    /**
     * 协议消息头
     * 和RpcConstant.MESSAGE_HEADER_LENGTH = 17; 关联
     */
    @Data
    public static class Header {
        /**
         * 魔数
         */
        private byte magic;

        /**
         * 协议版本
         */
        private byte version;

        /**
         * 序列化器
         */
        private byte serializer;

        /**
         * 消息类型 - 请求/响应
         */
        private byte type;

        /**
         * 状态
         */
        private byte status;

        /**
         * 请求 ID
         */
        private long requestId;

        /**
         * 消息体长度
         */
        private int bodyLength;
    }
}
