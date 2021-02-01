package com.cunjun.personal.emos.wx.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Created by CunjunWang on 2021/2/1.
 */
@Component
@Scope("prototype")
@PropertySource(value = "classpath:secret.properties")
public class EmailTask implements Serializable {

    @Autowired
    private JavaMailSender sender;

    @Value("${emos.email.system}")
    private String mailBox;

    @Async
    public void sendAsync(SimpleMailMessage message) {
        message.setFrom(mailBox);
        sender.send(message);
    }

}
