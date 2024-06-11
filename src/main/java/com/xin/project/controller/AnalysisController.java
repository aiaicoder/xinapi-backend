package com.xin.project.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xin.project.annotation.AuthCheck;
import com.xin.project.model.vo.InterfaceInfoVo;
import com.xin.project.common.BaseResponse;
import com.xin.project.common.ErrorCode;
import com.xin.project.common.ResultUtils;
import com.xin.project.exception.BusinessException;
import com.xin.project.mapper.UserInterfaceInfoMapper;
import com.xin.project.service.InterfaceInfoService;
import com.xin.xincommon.entity.InterfaceInfo;
import com.xin.xincommon.entity.UserInterfaceInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/analysis")
public class AnalysisController {
    @Resource
    private UserInterfaceInfoMapper  userInterfaceInfoMapper;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @GetMapping("/top/interface/invoke")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<InterfaceInfoVo>> getInvokeInterfaceTop(){
        List<UserInterfaceInfo> userInterfaceInfos = userInterfaceInfoMapper.getuserInterfaceInfos(3);
        Map<Long, List<UserInterfaceInfo>> userInterfaceInfoMap = userInterfaceInfos.stream().
                collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));
        if (CollectionUtil.isEmpty(userInterfaceInfoMap)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", userInterfaceInfoMap.keySet());
        List<InterfaceInfo> list = interfaceInfoService.list(queryWrapper);

        if (CollectionUtil.isEmpty(list)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        //关联查询collect
        List<InterfaceInfoVo> collect = list.stream().map(interfaceInfo -> {
            InterfaceInfoVo interfaceInfoVo = BeanUtil.copyProperties(interfaceInfo, InterfaceInfoVo.class);
            interfaceInfoVo.setTotalNum(userInterfaceInfoMap.get((interfaceInfo.getId())).get(0).getTotalNum());
            return interfaceInfoVo;
        }).collect(Collectors.toList());
        return ResultUtils.success(collect);
    }
}
