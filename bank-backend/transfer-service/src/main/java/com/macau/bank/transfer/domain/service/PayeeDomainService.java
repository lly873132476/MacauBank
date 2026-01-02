package com.macau.bank.transfer.domain.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macau.bank.common.core.enums.PayeeType;
import com.macau.bank.common.core.exception.BusinessException;
import com.macau.bank.transfer.common.result.TransferErrorCode;
import com.macau.bank.transfer.domain.entity.TransferPayeeBook;
import com.macau.bank.transfer.domain.repository.TransferPayeeBookRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收款人名册领域服务
 */
@Service
public class PayeeDomainService {

    @Resource
    private TransferPayeeBookRepository transferPayeeBookRepository;

    /**
     * 分页查询常用收款人
     */
    public Page<TransferPayeeBook> getPayeePage(String userNo, int current, int size) {
        // 构建查询条件
        TransferPayeeBook condition = new TransferPayeeBook();
        condition.setUserNo(userNo);
        condition.setPayeeType(PayeeType.FREQUENT);
        
        // 调用 Repository 查询列表
        List<TransferPayeeBook> list = transferPayeeBookRepository.query(condition, current, size);
        long total = transferPayeeBookRepository.count(condition);
        
        // 组装分页结果 (为了兼容上层调用，保持返回 Page 对象)
        Page<TransferPayeeBook> page = new Page<>(current, size);
        page.setRecords(list);
        page.setTotal(total);
        return page;
    }

    /**
     * 更新收款人信息
     */
    public void updatePayee(TransferPayeeBook payee) {
        TransferPayeeBook exist = transferPayeeBookRepository.findById(payee.getId());
        if (exist == null || !exist.getUserNo().equals(payee.getUserNo())) {
            throw new BusinessException(TransferErrorCode.PAYEE_NOT_FOUND);
        }
        payee.setUpdateTime(LocalDateTime.now());
        transferPayeeBookRepository.save(payee);
    }

    /**
     * 删除收款人
     */
    public void deletePayee(String userNo, Long id) {
        TransferPayeeBook exist = transferPayeeBookRepository.findById(id);
        if (exist == null) {
            throw new BusinessException(TransferErrorCode.PAYEE_NOT_FOUND);
        }
        if (!exist.getUserNo().equals(userNo)) {
            throw new BusinessException(TransferErrorCode.PAYEE_PERMISSION_DENIED);
        }
        transferPayeeBookRepository.deleteById(id);
    }

    /**
     * 添加收款人
     *
     * @param payee 收款人信息
     */
    public void addPayee(TransferPayeeBook payee) {
        // 1. 检查是否已存在相同的常用联系人
        TransferPayeeBook condition = new TransferPayeeBook();
        condition.setUserNo(payee.getUserNo());
        condition.setAccountNo(payee.getAccountNo());
        condition.setPayeeType(PayeeType.FREQUENT);
        
        long count = transferPayeeBookRepository.count(condition);

        if (count > 0) {
            throw new BusinessException(TransferErrorCode.PAYEE_ALREADY_EXISTS);
        }

        // 2. 插入新记录
        payee.setCreateTime(LocalDateTime.now());
        payee.setUpdateTime(LocalDateTime.now());
        transferPayeeBookRepository.save(payee);
    }

    /**
     * 更新或保存收款人历史
     */
    public void updatePayeeHistory(String userNo, String payeeName, String payeeAccountNo, String currency) {
        // 查找是否已存在
        TransferPayeeBook condition = new TransferPayeeBook();
        condition.setUserNo(userNo);
        condition.setAccountNo(payeeAccountNo);
        
        TransferPayeeBook payee = transferPayeeBookRepository.findOne(condition);

        if (payee == null) {
            // 新增历史收款人
            payee = new TransferPayeeBook();
            payee.setUserNo(userNo);
            payee.setPayeeName(payeeName);
            payee.setAccountNo(payeeAccountNo);
            payee.setCurrencyCode(currency);
            payee.setAliasName(payeeName);
            payee.setPayeeType(PayeeType.HISTORY); // 历史记录
            payee.setTotalTransCount(1);
            payee.setLastTransTime(LocalDateTime.now());
        } else {
            // 更新频率和时间
            payee.setTotalTransCount(payee.getTotalTransCount() + 1);
            payee.setLastTransTime(LocalDateTime.now());
        }
        transferPayeeBookRepository.save(payee);
    }
}