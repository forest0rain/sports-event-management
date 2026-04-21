package com.sports.platform.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Thymeleaf 日期时间格式化工具类
 * 提供 #dates.format, #calendars.format 类似的功能支持 Java 8 Time
 */
public class DateTimeFormatUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATETIME_SECOND_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter CHINESE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    /**
     * 格式化日期为 yyyy-MM-dd
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    /**
     * 格式化日期时间为 yyyy-MM-dd HH:mm
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : "";
    }

    /**
     * 格式化日期时间为 yyyy-MM-dd HH:mm:ss
     */
    public static String formatDateTimeSecond(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_SECOND_FORMATTER) : "";
    }

    /**
     * 格式化时间为 HH:mm
     */
    public static String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : "";
    }

    /**
     * 格式化日期为 MM-dd
     */
    public static String formatMonthDay(LocalDate date) {
        return date != null ? date.format(MONTH_DAY_FORMATTER) : "";
    }

    /**
     * 格式化日期为 yyyy年MM月dd日
     */
    public static String formatChineseDate(LocalDate date) {
        return date != null ? date.format(CHINESE_DATE_FORMATTER) : "";
    }

    /**
     * 格式化为 yyyy-MM-dd，可能为 null
     */
    public static String format(LocalDate date, String pattern) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化为 HH:mm，可能为 null
     */
    public static String format(LocalTime time, String pattern) {
        if (time == null) return "";
        return time.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化，可能为 null
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
}
