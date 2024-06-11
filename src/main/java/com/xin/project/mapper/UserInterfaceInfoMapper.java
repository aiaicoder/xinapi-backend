package com.xin.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xin.xincommon.entity.InterfaceInfo;
import com.xin.xincommon.entity.UserInterfaceInfo;

import java.util.List;

/**
* @author Administrator
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Mapper
* @createDate 2023-11-07 19:03:42
* @Entity com.xin.project.aop.model.entity.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {
    public List<UserInterfaceInfo> getuserInterfaceInfos(Integer limit);
}




