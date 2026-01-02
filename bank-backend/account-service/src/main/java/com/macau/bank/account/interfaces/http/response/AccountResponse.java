package com.macau.bank.account.interfaces.http.response;

import com.macau.bank.common.core.enums.AccountCategory;
import com.macau.bank.common.core.enums.AccountStatus;
import com.macau.bank.common.core.enums.AccountType;
import com.macau.bank.common.core.enums.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 账户简要信息响应
 * <p>
 * 用于首页账户列表展示，包含账户基本状态和余额概览
 * </p>
 */
@Data
@Schema(description = "账户简要信息响应")
public class AccountResponse implements Serializable {
    
    @Schema(description = "账户ID (内部主键)", example = "10001")
    private Long id;

    @Schema(description = "用户编号", example = "U20231027001")
    private String userNo;

    @Schema(description = "银行账号 (对外展示)", example = "888800012345")
    private String accountNo;

    @Schema(description = "关联卡号 (如有)", example = "6225888800012345")
    private String cardNumber;

    @Schema(description = "账户大类 (1:个人, 2:企业)", example = "1")
    private AccountCategory accountCategory;

    @Schema(description = "账户类型 (1:多币种储蓄, 2:往来)", example = "1")
    private AccountType accountType;

    @Schema(description = "账户状态", example = "1")
    private AccountStatus status;

    @Schema(description = "风险等级", example = "LOW")
    private RiskLevel riskLevel;

    @Schema(description = "开户网点代码", example = "BR001")
    private String openBranchCode;

    @Schema(description = "开户网点名称", example = "澳门总行营业部")
    private String openBranchName;

    @Schema(description = "开户时间", example = "2023-10-27 10:00:00")
    private String createTime;

    @Schema(description = "多币种余额列表")
    private List<AccountBalanceResponse> balances;
}
