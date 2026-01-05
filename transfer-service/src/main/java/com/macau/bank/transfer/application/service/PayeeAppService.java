package com.macau.bank.transfer.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macau.bank.transfer.application.command.AddPayeeCmd;
import com.macau.bank.transfer.application.command.UpdatePayeeCmd;
import com.macau.bank.transfer.application.result.PayeeResult;

/**
 * 收款人应用服务
 */
public interface PayeeAppService {

    /**
     * 分页查询收款人
     */
    Page<PayeeResult> getPayeePage(String userNo, int current, int size);

    /**
     * 添加收款人
     */
    void addPayee(AddPayeeCmd cmd);

    /**
     * 更新收款人
     */
    void updatePayee(UpdatePayeeCmd cmd);

    /**
     * 删除收款人
     */
    void deletePayee(String userNo, Long id);
}
