package com.rpc.rpc_demo.protocol;

import lombok.Getter;

/**
 * @author jiahao.liu
 * @description 消息的状态的枚举
 * @date 2025/03/08 19:36
 */
@Getter
public enum ProtocolMessageStatusEnum {
    OK("成功", 0),

    BAD_REQUEST("badRequest", 40),

    BAD_RESPONSE("badResponse", 50),
    ;
    private final String text;

    private final int value;

    ProtocolMessageStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     */
    public static ProtocolMessageStatusEnum getEnum(int value) {
        for (ProtocolMessageStatusEnum statusEnum : values()) {
            if (statusEnum.getValue() == value) {
                return statusEnum;
            }
        }
        return null;
    }
}