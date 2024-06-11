package com.xin.project.service;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class UserInterfaceInfoTest {
    @Resource
    private UserInterfaceInfoService service;

    @Test
    void invoke() {
        boolean b = service.invokeCount(1, 3);
        Assertions.assertTrue(b);
    }
}
