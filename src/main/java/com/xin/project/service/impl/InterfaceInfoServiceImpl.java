package com.xin.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.project.exception.BusinessException;
import com.xin.project.mapper.InterfaceInfoMapper;
import com.xin.project.service.InterfaceInfoService;
import com.xin.project.common.ErrorCode;
import com.xin.xincommon.entity.InterfaceInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 * @description 针对表【interface_info(接口表名)】的数据库操作Service实现
 * @createDate 2023-10-24 11:12:09
 */
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
        implements InterfaceInfoService {

    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String name = interfaceInfo.getName();
        String description = interfaceInfo.getDescription();
        String url = interfaceInfo.getUrl();
        String requestHeader = interfaceInfo.getRequestHeader();
        String responseHeader = interfaceInfo.getResponseHeader();
        String method = interfaceInfo.getMethod();
        Long userId = interfaceInfo.getUserId();

        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(name)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (StringUtils.isNotBlank(name) && name.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
        }

        if (StringUtils.isNotBlank(description) && description.length() > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "简介过长");
        }

        if (StringUtils.isBlank(url)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "url不能为空");
        }

        if (StringUtils.isBlank(method) && ("GET".equals(method) || "POST".equals(method))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求方式不合法");
        }

        if (StringUtils.isBlank(requestHeader) || StringUtils.isBlank(responseHeader)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求头或响应头不符合要求");
        }
    }
}




