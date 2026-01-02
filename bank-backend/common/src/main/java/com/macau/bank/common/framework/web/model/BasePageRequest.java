package com.macau.bank.common.framework.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 分页请求基类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "分页请求基础参数")
public class BasePageRequest extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "页码 (从1开始)", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", example = "10")
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Integer pageSize = 10;

    /**
     * 获取偏移量 (MyBatis 分页使用)
     */
    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
