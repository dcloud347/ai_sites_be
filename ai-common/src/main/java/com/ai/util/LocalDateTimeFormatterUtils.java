package com.ai.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class LocalDateTimeFormatterUtils {

    /**
     * 默认时间日期格式字符串
     * yyyy-MM-dd HH:mm:ss
     * eg.
     * 2022-06-15 22:06:29
     */
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String localDateTimeToString(LocalDateTime localDateTime) {
        return localDateTime.format(DEFAULT_FORMATTER);
    }

}

