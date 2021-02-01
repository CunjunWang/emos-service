package com.cunjun.personal.emos.wx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by CunjunWang on 2021/2/1.
 */
@Configuration
public class ThreadPoolConfig {

    @Value("${emos.thread.core-pool-size}")
    private Integer corePoolSize;

    @Value("${emos.thread.max-pool-size}")
    private Integer maxPoolSize;

    @Value("${emos.thread.max-pool-size}")
    private Integer queueCapacity;

    @Value("${emos.thread.keep-alive-secs}")
    private Integer keepAliveSecs;

    @Value("${emos.thread.name-prefix}")
    private String prefix;

    @Bean("AsyncTaskExecutor")
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSecs);
        executor.setThreadNamePrefix(prefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
