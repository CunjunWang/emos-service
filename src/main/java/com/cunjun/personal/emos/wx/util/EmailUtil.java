package com.cunjun.personal.emos.wx.util;

import cn.hutool.core.date.DateUtil;
import com.cunjun.personal.emos.wx.db.dao.TbUserDao;
import com.cunjun.personal.emos.wx.task.EmailTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by CunjunWang on 2021/2/1.
 */
@Slf4j
@Component
@PropertySource(value = {"classpath:application.yml", "classpath:secret.properties"})
public class EmailUtil {

    @Value("${emos.email.hr}")
    private String hrEmail;

    @Value("${emos.email.pandemic.subject}")
    private String emailSubject;

    @Value("${emos.email.pandemic.content}")
    private String emailContent;

    @Autowired
    private EmailTask emailTask;

    @Autowired
    private TbUserDao userDao;

    public void sendMessage(Integer userId, String address) {
        HashMap<String, String> map = userDao.searchNameAndDept(userId);
        String name = map.get("name");
        String deptName = map.get("dept_name");
        deptName = deptName != null ? deptName : "";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(hrEmail);
        message.setSubject(String.format(emailSubject, name));
        message.setText(String.format(emailContent, deptName, name,
                DateUtil.format(new Date(), "yyyy年MM月dd日"), address));
        emailTask.sendAsync(message);
    }
}
