package com.macau.bank.common.core.constant;

public class RegexPatterns {
    /** 手机号正则 */
    public static final String MOBILE = "^1[3-9]\\d{9}$";
    /** 身份证正则 (15或18位) */
    public static final String ID_CARD = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)";
    /** 密码强度：字母+数字，8位以上 */
    public static final String PASSWORD_COMPLEX = "^(?=.*[A-Za-z])(?=.*\\d).{8,20}$";
}