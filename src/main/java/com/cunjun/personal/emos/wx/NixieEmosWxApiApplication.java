package com.cunjun.personal.emos.wx;

import cn.hutool.core.util.StrUtil;
import com.cunjun.personal.emos.wx.common.SystemConstants;
import com.cunjun.personal.emos.wx.db.dao.SysConfigDao;
import com.cunjun.personal.emos.wx.db.pojo.SysConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

@Slf4j
@ServletComponentScan
@SpringBootApplication
public class NixieEmosWxApiApplication {

    @Value("${emos.temp-image-folder}")
    private String tempImageFolder;

    @Autowired
    private SysConfigDao sysConfigDao;

    @Autowired
    private SystemConstants constants;

    public static void main(String[] args) {
        SpringApplication.run(NixieEmosWxApiApplication.class, args);
    }

    @PostConstruct
    public void init() {
        List<SysConfig> list = sysConfigDao.selectAllParam();
        list.forEach(config -> {
            String key = config.getParamKey();
            key = StrUtil.toCamelCase(key);
            String value = config.getParamValue();
            try {
                Field field = constants.getClass().getDeclaredField(key);
                field.set(constants, value);
            } catch (Exception e) {
                log.error("执行异常, ", e);
            }
        });

        // 创建临时文件夹
        new File(tempImageFolder).mkdirs();
    }

}
