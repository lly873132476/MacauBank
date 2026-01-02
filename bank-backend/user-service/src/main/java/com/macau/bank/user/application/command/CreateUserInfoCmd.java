package com.macau.bank.user.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建用户信息指令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserInfoCmd implements Serializable {
    private String userNo;
    private String userName;
    private String mobile;
}