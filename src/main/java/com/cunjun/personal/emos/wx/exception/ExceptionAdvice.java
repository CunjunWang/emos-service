package com.cunjun.personal.emos.wx.exception;

import com.cunjun.personal.emos.wx.common.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Created by CunjunWang on 2021/1/27.
 */
@Slf4j
@Component
@RestControllerAdvice
public class ExceptionAdvice {

    @ResponseBody
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultData exceptionHandler(Exception e) {
        log.error("执行异常, ", e);
        String msg;

        if (e instanceof MethodArgumentNotValidException)
            msg = ((MethodArgumentNotValidException) e)
                    .getBindingResult().getFieldError().getDefaultMessage();
        else if (e instanceof EmosException)
            msg = ((EmosException) e).getMsg();
        else if (e instanceof UnauthorizedException)
            msg = "不具备相关权限";
        else
            msg = "后端执行异常";

        return ResultData.ok(msg);
    }

}
