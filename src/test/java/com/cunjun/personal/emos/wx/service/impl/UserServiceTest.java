package com.cunjun.personal.emos.wx.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Created by CunjunWang on 2021/1/28.
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {

    @Autowired
    private UserService userService;

//    @Test
//    public void testGetOpenId() {
//        String code = "0413CQkl2dYyp64tJKml2steMo23CQkE";
//        String openId = userService.getOpenId(code);
//        log.info("openId: {}", openId);
//    }
}