package com.macau.bank.account.interfaces.http.dto;

import com.macau.bank.common.framework.web.model.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 后台开通币种账户请求参数
 * <p>用于管理员在后台为指定账户开通新的币种余额记录。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "后台开通币种账户请求参数")
public class AdminOpenCurrencyRequest extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "目标账号", example = "6222020100000001")
    @NotBlank(message = "账号不能为空")
    private String accountNo;

    @Schema(description = "待开通的币种代码 (必须在配置中存在)", example = "USD")
    @NotBlank(message = "币种代码不能为空")
    private String currencyCode;

    @Schema(description = "管理员密钥", example = "MacauBankAdmin888")
    @NotBlank(message = "管理员密钥不能为空")
    private String adminSecret;
}