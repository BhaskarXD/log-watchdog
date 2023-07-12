package spr.graylog.analytics.logwatchdog.util;

import org.springframework.core.task.TaskRejectedException;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class CustomRejectionPolicy implements RejectedExecutionHandler {
    private static final String TASK_REJECTED_BY_CUSTOM_EXECUTOR_MESSAGE = "TASK_REJECTED_BY_CUSTOM_EXECUTOR_BECAUSE_MAX_POOL_SIZE_REACHED_AND_0_QUEUE_SIZE";

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        throw new TaskRejectedException(TASK_REJECTED_BY_CUSTOM_EXECUTOR_MESSAGE);
    }
}