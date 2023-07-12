package spr.graylog.analytics.logwatchdog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import spr.graylog.analytics.logwatchdog.util.CustomRejectionPolicy;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AnomalyDetectionAsyncConfig {
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 0;

    @Bean(name = "AnomalyDetectionMultiThreadingBean")
    public Executor getThreadPoolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("AnomalyDetectionThread::");
        executor.setRejectedExecutionHandler(new CustomRejectionPolicy());
        executor.initialize();
        return executor;
    }
}