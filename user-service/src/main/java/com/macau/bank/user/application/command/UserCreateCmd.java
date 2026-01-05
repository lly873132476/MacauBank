package com.macau.bank.user.application.command;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserCreateCmd implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户编号
     */
    private String userNo;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 手机号
     */
    private String mobile;


}
