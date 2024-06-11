package com.xin.project.model.vo;

import com.xin.project.model.entity.Post;
import com.xin.xincommon.entity.InterfaceInfo;
import io.swagger.models.auth.In;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口调用视图
 *
 * @author yupi
 * @TableName product
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceInfoVo extends InterfaceInfo {

    /**
     *剩余调用次数
     */
    private Integer leftNum;

    /**
     * 总调用次数
     */
    private Integer totalNum;


    private static final long serialVersionUID = 1L;
}