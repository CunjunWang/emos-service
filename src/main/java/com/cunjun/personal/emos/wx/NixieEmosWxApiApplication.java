package com.cunjun.personal.emos.wx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class NixieEmosWxApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NixieEmosWxApiApplication.class, args);
    }

}
