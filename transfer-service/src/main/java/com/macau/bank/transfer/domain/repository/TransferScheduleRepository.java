package com.macau.bank.transfer.domain.repository;

import com.macau.bank.transfer.domain.entity.TransferSchedule;

import java.util.List;

public interface TransferScheduleRepository {
    boolean save(TransferSchedule schedule);
    TransferSchedule findById(Long id);
    List<TransferSchedule> findByUserNo(String userNo);
}
