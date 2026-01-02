package com.macau.bank.forex.application.result;

import com.macau.bank.forex.common.enums.ForexTradeStatusEnum;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ExchangeResult implements Serializable {
    private String txnId;
    private BigDecimal dealRate;
    private String sellCurrency;
    private BigDecimal sellAmount;
    private String buyCurrency;
    private BigDecimal buyAmount;
    private ForexTradeStatusEnum status;
    private LocalDateTime transTime;
}
