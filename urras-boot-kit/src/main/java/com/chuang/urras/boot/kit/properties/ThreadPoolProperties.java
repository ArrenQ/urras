package com.chuang.urras.boot.kit.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "thread.pool")
@Data
public class ThreadPoolProperties {

    private int workQueueCapacity = 0;

    private int coreSize = Runtime.getRuntime().availableProcessors();

    private int maximumSize = Runtime.getRuntime().availableProcessors() * 10;

    private int keepAliveTime = 60;

    private String threadNamePrefix = "task-pool";

    private String schedulerNamePrefix = "task-scheduler";

    private int schedulerPoolSize = 2;


}
