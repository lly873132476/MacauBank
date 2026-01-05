package com.macau.bank.account.interfaces.http.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 资产总览响应
 * <p>
 * 首页展示用户的总资产估值及账户列表
 * </p>
 */
@Data
@Schema(description = "个人资产总览")
public class AssetSummaryResponse implements Serializable {

    @Schema(description = "总资产估值 (等值澳门元)", example = "158800.50")
    private BigDecimal totalMopValue;
    
    @Schema(description = "名下账户列表")
    private List<AccountResponse> accounts;
}
