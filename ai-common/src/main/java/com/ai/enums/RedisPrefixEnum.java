package com.ai.enums;

import lombok.Getter;

@Getter
public enum RedisPrefixEnum {

    /**
     * 列表redis中的各种前缀
     */
    // 用户的token
    USER_TOKEN("userToken:"),
    //管理员的token
    ADMIN_TOKEN("adminToken:");

    private final String prefix;

    RedisPrefixEnum(String prefix) {
        this.prefix = prefix;
    }

}
