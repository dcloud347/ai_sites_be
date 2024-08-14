package com.ai.enums;

import lombok.Getter;

/**
 * @author 刘晨
 */

@Getter
public enum RedisPrefixEnum {
    /**
     * 列表redis中的各种前缀
     */
    ROBOT_SESSION("robotSession:");
    private final String prefix;

    RedisPrefixEnum(String prefix) {
        this.prefix = prefix;
    }

}
