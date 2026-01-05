package com.macau.bank.api.account.response;

import com.macau.bank.common.core.enums.AccountCategory;
import com.macau.bank.common.core.enums.AccountStatus;
import com.macau.bank.common.core.enums.AccountType;
import com.macau.bank.common.core.enums.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 账户信息 RPC 响应
 * <p>
 * 用于跨服务传输账户主档及余额信息
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfoRpcResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "账户ID")
    private Long id;

    @Schema(description = "用户编号")
    private String userNo;

    @Schema(description = "银行账号")
    private String accountNo;

    @Schema(description = "账号姓名")
    private String accountName;

    @Schema(description = "账户主币种 (默认)")
    private String currencyCode;

    @Schema(description = "卡号")
    private String cardNumber;

    @Schema(description = "账户分类")
    private AccountCategory accountCategory;

    @Schema(description = "账户类型")
    private AccountType accountType;

    @Schema(description = "状态")
    private AccountStatus status;

    @Schema(description = "风险等级")
    private RiskLevel riskLevel;

    @Schema(description = "开户行代码")
    private String openBranchCode;

    @Schema(description = "开户行名称")
    private String openBranchName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "余额列表")
    private List<AccountBalanceRpcResponse> balances;
}
