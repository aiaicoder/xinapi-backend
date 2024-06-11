package com.xin.project.service;

import com.xin.project.exception.BusinessException;
import com.xin.project.service.impl.UserInterfaceInfoServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserInterfaceInfoServiceImplTest {

    
    @Autowired
    private UserInterfaceInfoServiceImpl userInterfaceInfoService;

    
    @Test
    public void testGetLeftNum_InvalidUserId_ThrowsBusinessException() {
        long interfaceInfoId = 2;
        long userId = 3;
        int leftNum = userInterfaceInfoService.getLeftNum(interfaceInfoId, userId);
        System.out.println(leftNum);
    }
}
