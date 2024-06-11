package com.xin.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xin.project.model.dto.userInterfaceInfo.UpdateUserInterfaceInfoDTO;
import com.xin.xincommon.entity.UserInterfaceInfo;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
* @createDate 2023-11-07 19:03:42
*/
@Service
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    /**
     * 有效的用户界面信息方法，接受一个UserInterfaceInfo对象和一个布尔值作为参数
     * @param userInterfaceInfo 用户界面信息对象
     * @param b 布尔值参数
     */
    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean b);

    /**
     * 调用次数方法，接受一个长整型接口ID和一个长整型用户ID作为参数，返回一个布尔值
     * @param interfaceInfoId 接口ID
     * @param userId 用户ID
     * @return 布尔值表示调用是否成功
     */
    boolean invokeCount(long interfaceInfoId, long userId);

    int getLeftNum(long interfaceInfoId, long userId);

    /**
     * 更新用户接口信息
     * @param updateUserInterfaceInfoDTO
     * @return
     */
    boolean UpdateUserInterfaceInfo(UpdateUserInterfaceInfoDTO updateUserInterfaceInfoDTO);
}
