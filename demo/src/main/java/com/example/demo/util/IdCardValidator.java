package com.example.demo.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 中国大陆 18 位身份证号码校验工具。
 * 使用 GB 11643-1999 标准的校验位算法，无需第三方接口。
 */
public class IdCardValidator {

    /** 18 位身份证加权因子 */
    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /** 校验码字符表 */
    private static final char[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    /**
     * 校验 18 位身份证号码是否合法。
     * @param idCard 18 位身份证号（末位可为 X）
     * @return 合法返回 null，不合法返回错误描述
     */
    public static String validate(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            return "身份证号必须为18位";
        }

        String num = idCard.toUpperCase();

        // 前 17 位必须为数字
        for (int i = 0; i < 17; i++) {
            if (!Character.isDigit(num.charAt(i))) {
                return "身份证号前17位必须为数字";
            }
        }

        // 第 18 位校验
        char last = num.charAt(17);
        if (!Character.isDigit(last) && last != 'X') {
            return "身份证号末位必须为数字或X";
        }

        // 校验出生日期
        String birthStr = num.substring(6, 14);
        try {
            LocalDate birth = LocalDate.parse(birthStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            if (birth.isAfter(LocalDate.now())) {
                return "出生日期不能晚于今天";
            }
        } catch (DateTimeParseException e) {
            return "身份证号中出生日期无效";
        }

        // 校验位计算
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (num.charAt(i) - '0') * WEIGHTS[i];
        }
        char expectedCheckCode = CHECK_CODES[sum % 11];

        if (last != expectedCheckCode) {
            return "身份证号校验位错误，请检查号码";
        }

        return null; // 校验通过
    }

    /**
     * 从 18 位身份证号提取出生日期。
     */
    public static String extractBirthDate(String idCard) {
        if (idCard == null || idCard.length() < 14) return "";
        return idCard.substring(6, 10) + "-" + idCard.substring(10, 12) + "-" + idCard.substring(12, 14);
    }

    /**
     * 从 18 位身份证号提取性别（第 17 位奇数为男，偶数为女）。
     */
    public static String extractGender(String idCard) {
        if (idCard == null || idCard.length() < 17) return "未知";
        int digit = idCard.charAt(16) - '0';
        return (digit % 2 == 1) ? "男" : "女";
    }
}
