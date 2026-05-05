package com.sports.platform.util;

/**
 * 数据脱敏工具类
 * 对敏感信息进行部分隐藏处理，保护用户隐私
 */
public class DesensitizationUtil {

    /**
     * 手机号脱敏：保留前3位和后4位
     * 示例：13812345678 → 138****5678
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 身份证号脱敏：保留前3位和后4位
     * 示例：330102199001011234 → 330***********1234
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        int len = idCard.length();
        StringBuilder masked = new StringBuilder(idCard.substring(0, 3));
        for (int i = 3; i < len - 4; i++) {
            masked.append('*');
        }
        masked.append(idCard.substring(len - 4));
        return masked.toString();
    }

    /**
     * 邮箱脱敏：保留前2位和@后的域名
     * 示例：zhangsan@qq.com → zh****@qq.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String name = parts[0];
        if (name.length() <= 2) {
            return name + "***@" + parts[1];
        }
        return name.substring(0, 2) + "***@" + parts[1];
    }

    /**
     * 姓名脱敏：保留姓，名用*代替
     * 示例：张三 → 张*，欧阳修 → 欧阳*
     */
    public static String maskName(String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        // 复姓处理
        String[] doubleSurnames = {"欧阳", "司马", "上官", "诸葛", "东方", "皇甫", "尉迟", "公孙", "慕容", "端木"};
        for (String ds : doubleSurnames) {
            if (name.startsWith(ds)) {
                return ds + "***";
            }
        }
        return name.charAt(0) + "***";
    }
}
