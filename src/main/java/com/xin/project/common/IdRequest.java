package com.xin.project.common;

import lombok.Data;

import java.io.Serializable;

/**
 * ID请求
 *
 * @author xin
 */
@Data
public class IdRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}