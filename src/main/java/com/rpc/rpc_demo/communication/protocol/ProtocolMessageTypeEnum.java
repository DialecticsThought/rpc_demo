package com.rpc.rpc_demo.communication.protocol;

import lombok.Getter;

/**
 * @author jiahao.liu
 * @description 消息的类型
 * @date 2025/03/08 19:37
 */
@Getter
public enum ProtocolMessageTypeEnum {
    REQUEST(0),
    RESPONSE(1),
    HEAT_BEAT(2),
    OTHER(3);


    private final int key;

    ProtocolMessageTypeEnum(int key) {
        this.key = key;
    }

    /**
     * 根据key获取枚举
     */
    public static ProtocolMessageTypeEnum getEnum(int key) {
        for (ProtocolMessageTypeEnum typeEnum : values()) {
            if (typeEnum.getKey() == key) {
                return typeEnum;
            }
        }
        return null;
    }
}
