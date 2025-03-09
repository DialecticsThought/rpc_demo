package com.rpc.rpc_demo.communication.protocol;

/**
 * @author jiahao.liu
 * @description
 * @date 2025/03/08 19:38
 */
public class ProtocolConstant {
    /**
     * 消息头长度 这个消息头长度与消息头的定义有关 ，如果定义出现变化 这个长度需要改
     */
    public static int MESSAGE_HEADER_LENGTH = 17;

    /**
     * 魔数
     */
    public static byte PROTOCOL_MAGIC = 0x01;

    /**
     * 协议版本
     */
    public static byte PROTOCOL_VERSION = 0x01;
}
