package com.macau.bank.user.interfaces.http.request;

import com.macau.bank.common.framework.web.model.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 更新用户资料请求
 * <p>
 * 仅允许修改非核心身份信息（如地址、职业等），核心身份信息变更需走人工审核流程
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "更新用户资料请求对象")
public class UpdateProfileRequest extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "职业描述", example = "Manager")
    @Size(max = 100, message = "职业描述过长")
    private String occupation;

    @Schema(description = "地区 (如: 澳门半岛/氹仔/路环)", example = "澳门半岛")
    @Size(max = 20, message = "地区过长")
    private String addressRegion;

    @Schema(description = "详细地址 (街道/大厦/单位)", example = "水坑尾街100号")
    @Size(max = 100, message = "详细地址过长")
    private String addressDetail;
}