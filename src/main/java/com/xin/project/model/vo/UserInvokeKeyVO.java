package com.xin.project.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserInvokeKeyVO implements Serializable {
    private String accessKey;
    private String secretKey;

}
