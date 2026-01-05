package com.macau.bank.forex.interfaces.http.assembler;

import cn.hutool.core.util.RandomUtil;
import com.macau.bank.forex.domain.entity.ExchangeRate;
import com.macau.bank.forex.interfaces.http.response.ExchangeRateReferenceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CurrencyWebAssembler {

    @Mapping(target = "currencyPair", expression = "java(entity.getBaseCurrency() + \"/\" + entity.getTargetCurrency())")
    @Mapping(target = "buyRate", source = "bankBuyRate")
    @Mapping(target = "sellRate", source = "bankSellRate")
    @Mapping(target = "changePercent", source = "entity", qualifiedByName = "mockChangePercent")
    @Mapping(target = "updateTime", source = "createTime", qualifiedByName = "formatTime")
    ExchangeRateReferenceResponse toReferenceResponse(ExchangeRate entity);

    List<ExchangeRateReferenceResponse> toReferenceResponseList(List<ExchangeRate> entityList);

    @Named("formatTime")
    default String formatTime(LocalDateTime time) {
        if (time == null) return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    @Named("mockChangePercent")
    default String mockChangePercent(ExchangeRate entity) {
        // 模拟 -0.50% 到 +0.50% 之间的涨跌幅
        double random = RandomUtil.randomDouble(-0.5, 0.5);
        BigDecimal bd = BigDecimal.valueOf(random).setScale(2, RoundingMode.HALF_UP);
        return (bd.compareTo(BigDecimal.ZERO) > 0 ? "+" : "") + bd + "%";
    }
}
