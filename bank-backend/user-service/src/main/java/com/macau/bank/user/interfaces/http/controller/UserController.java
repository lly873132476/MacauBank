package com.macau.bank.user.interfaces.http.controller;

import com.macau.bank.common.core.result.Result;
import com.macau.bank.common.framework.web.annotation.CurrentUser;
import com.macau.bank.user.application.command.UpdateProfileCmd;
import com.macau.bank.user.application.command.UserCertificationCmd;
import com.macau.bank.user.application.result.UserProfileResult;
import com.macau.bank.user.application.service.UserInfoAppService;
import com.macau.bank.user.interfaces.http.assembler.UserWebAssembler;
import com.macau.bank.user.interfaces.http.request.UpdateProfileRequest;
import com.macau.bank.user.interfaces.http.request.UserCertificationRequest;
import com.macau.bank.user.interfaces.http.response.UserProfileResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户中心 HTTP 接口
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserInfoAppService userInfoAppService;

    @Resource
    private UserWebAssembler userWebAssembler;

    /**
     * 获取当前用户详情 (个人资料)
     */
    @GetMapping("/profile/me")
    public Result<UserProfileResponse> getProfile(@CurrentUser String userNo) {
        UserProfileResult result = userInfoAppService.getUserProfile(userNo);
        return Result.success(userWebAssembler.toResponse(result));
    }

    /**
     * 实名认证申请 (上传证件信息)
     */
    @PostMapping("/certify")
    public Result<Void> certify(@RequestBody @Validated UserCertificationRequest request) {
        UserCertificationCmd cmd = userWebAssembler.toCmd(request);
        userInfoAppService.certifyUser(cmd);
        return Result.success();
    }

    /**
     * 更新补充资料
     */
    @PostMapping("/profile/update")
    public Result<Void> updateProfile(@RequestBody @Validated UpdateProfileRequest request) {
        UpdateProfileCmd cmd = userWebAssembler.toCmd(request);
        userInfoAppService.updateUserProfile(cmd);
        return Result.success();
    }
}
