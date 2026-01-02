package com.macau.bank.transfer.domain.repository;

import com.macau.bank.transfer.domain.entity.TransferPayeeBook;

import java.util.List;

public interface TransferPayeeBookRepository {
    boolean save(TransferPayeeBook payeeBook);
    TransferPayeeBook findById(Long id);
    List<TransferPayeeBook> findByUserNo(String userNo);
    boolean deleteById(Long id);
    
    // New methods for Domain Service needs
    long count(TransferPayeeBook condition);
    TransferPayeeBook findOne(TransferPayeeBook condition);
    // 暂不引入 Mybatis-Plus IPage 依赖到 Domain 接口，使用 List 返回，或者封装 Page 对象。
    // 为简化重构，此处先返回 List，App 层处理分页或透传参数。
    // 但 PayeeDomainService 原代码用了 selectPage。
    // 决策：Domain 层不应感知 HTTP 分页，应接收 page/size 返回 List + Total。
    // 但为了兼容现有逻辑，我们先暴露 List 查询能力。
    List<TransferPayeeBook> query(TransferPayeeBook condition, int page, int size);
}
