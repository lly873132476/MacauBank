package com.macau.bank.transfer.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macau.bank.common.core.enums.PayeeType;
import com.macau.bank.common.core.enums.YesNo;
import com.macau.bank.transfer.application.assembler.TransferAssembler;
import com.macau.bank.transfer.application.command.AddPayeeCmd;
import com.macau.bank.transfer.application.command.UpdatePayeeCmd;
import com.macau.bank.transfer.application.result.PayeeResult;
import com.macau.bank.transfer.application.service.PayeeAppService;
import com.macau.bank.transfer.domain.entity.TransferPayeeBook;
import com.macau.bank.transfer.domain.service.PayeeDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 收款人应用服务实现
 */
@Slf4j
@Service
public class PayeeAppServiceImpl implements PayeeAppService {

    @Resource
    private PayeeDomainService payeeDomainService;

    @Resource
    private TransferAssembler transferAssembler;

    @Override
    public Page<PayeeResult> getPayeePage(String userNo, int current, int size) {
        Page<TransferPayeeBook> page = payeeDomainService.getPayeePage(userNo, current, size);
        
        Page<PayeeResult> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(transferAssembler.toPayeeResultList(page.getRecords()));
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPayee(AddPayeeCmd cmd) {
        // 1. 【拆包】 Cmd -> Entity
        TransferPayeeBook payeeBook = transferAssembler.toPayeeEntity(cmd);
        
        // 2. 设置默认值
        payeeBook.setPayeeType(PayeeType.FREQUENT); // 常用联系人
        payeeBook.setIsTop(YesNo.NO);
        payeeBook.setIsInternal(YesNo.NO);
        payeeBook.setTotalTransCount(0);

        // 3. 【办事】 调用领域服务
        payeeDomainService.addPayee(payeeBook);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePayee(UpdatePayeeCmd cmd) {
        TransferPayeeBook entity = transferAssembler.toPayeeEntity(cmd);
        payeeDomainService.updatePayee(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePayee(String userNo, Long id) {
        payeeDomainService.deletePayee(userNo, id);
    }
}
