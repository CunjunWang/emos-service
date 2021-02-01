package com.cunjun.personal.emos.wx.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by CunjunWang on 2021/2/1.
 */
@Slf4j
@Component
public class JSoupUtil {

    public Element getDocument(String url) {
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("获取HTML异常");
            throw new RuntimeException("获取HTML异常");
        }
    }

}
