package com.xin.project.service.impl.inner;

import com.xin.project.service.UserInterfaceInfoService;
import com.xin.xincommon.server.InnerUserInterfaceInfoService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;


@DubboService
public class InnerUserInterfaceInfoServiceImpl implements InnerUserInterfaceInfoService {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;
    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        return userInterfaceInfoService.invokeCount(interfaceInfoId,userId);
    }

    @Override
    public int getLeftNum(long interfaceInfoId, long userId) {
        return userInterfaceInfoService.getLeftNum(interfaceInfoId,userId);
    }
}
