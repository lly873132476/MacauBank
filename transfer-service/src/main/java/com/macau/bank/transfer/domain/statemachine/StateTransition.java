package com.macau.bank.transfer.domain.statemachine;

import com.macau.bank.common.core.enums.TransferStatus;
import com.macau.bank.transfer.domain.pipeline.TransferPhaseEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

// 定义一个简单的内部类或DTO来承载返回值
    @Data
    @AllArgsConstructor
    public class StateTransition {
        private List<TransferPhaseEnum> handlers; // 要跑的积木
        private TransferStatus nextStatus;        // 跑完变啥样
    }