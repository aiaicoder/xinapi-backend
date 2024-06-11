package com.xin.project.model.dto.interfaceinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 调用请求
 *
 * @author xin
 * @TableName product
 */
@Data
public class InterfaceInfoInvokeRequest implements Serializable {

    /**
     * 接口id,不可改
     */
    private Integer id;

    /**
     * 请求参数
     */
    private String userRequestParams;

}