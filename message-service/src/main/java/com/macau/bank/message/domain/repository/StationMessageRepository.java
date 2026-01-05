package com.macau.bank.message.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.macau.bank.message.domain.entity.StationMessage;

import java.util.List;

/**
 * 站内消息仓储接口
 * <p>
 * 归属：Domain 层
 * 职责：定义站内消息数据访问契约
 */
public interface StationMessageRepository {

    /**
     * 保存消息
     */
    void save(StationMessage message);

    /**
     * 根据业务ID和用户编号统计消息数量（幂等性校验）
     */
    Long countByBizIdAndUserNo(String bizId, String userNo);

    /**
     * 分页查询用户消息
     */
    IPage<StationMessage> pageByUserNo(String userNo, int page, int pageSize);

    /**
     * 更新消息已读状态
     * 
     * @return 更新的行数
     */
    int updateReadStatus(String bizId, String userNo);

    /**
     * 统计未读消息数量
     */
    Long countUnread(String userNo);

    /**
     * 查询用户全量消息列表
     */
    List<StationMessage> findByUserNo(String userNo);
}
