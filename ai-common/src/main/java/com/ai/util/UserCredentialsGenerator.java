package com.ai.util;

import java.security.SecureRandom;
import java.util.Random;

public class UserCredentialsGenerator {

    // 字符池，用于生成用户名和密码
    private static final String USERNAME_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String PASSWORD_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_+=<>?";

    // 生成随机字符串的方法
    private static String generateRandomString(String characterSet, int length) {
        Random random = new SecureRandom();
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(characterSet.charAt(random.nextInt(characterSet.length())));
        }
        return result.toString();
    }

    // 生成随机用户名的方法
    public static String generateUsername(int length) {
        return generateRandomString(USERNAME_CHARS, length);
    }

    // 生成随机密码的方法
    public static String generatePassword(int length) {
        return generateRandomString(PASSWORD_CHARS, length);
    }
}

