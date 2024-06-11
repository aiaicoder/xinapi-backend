package com.xin.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xin.xincommon.entity.InterfaceInfo;

/**
* @author Administrator
* @description 针对表【interface_info(接口表名)】的数据库操作Service
* @createDate 2023-10-24 11:12:09
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);
}
