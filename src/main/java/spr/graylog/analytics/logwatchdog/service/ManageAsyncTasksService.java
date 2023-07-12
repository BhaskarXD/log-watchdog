package spr.graylog.analytics.logwatchdog.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ManageAsyncTasksService {
    private static final String NEW_LOG_MONITORING_TASK_ADDED = "NEW_LOG_MONITORING_TASK_ADDED";
    private static final String LOG_MONITORING_TASK_ALREADY_EXISTS = "LOG_MONITORING_TASK_ALREADY_EXISTS";
    private static final String LOG_MONITORING_TASK_DOES_NOT_EXISTS = "LOG_MONITORING_TASK_DOES_NOT_EXISTS";
    private static final String LOG_MONITORING_TASK_TERMINATED = "LOG_MONITORING_TASK_TERMINATED";
    private final AsyncLogMonitoringService asyncLogMonitoringService;

    public ManageAsyncTasksService(AsyncLogMonitoringService asyncLogMonitoringService) {
        this.asyncLogMonitoringService = asyncLogMonitoringService;
    }

    public Map<Map<String, String>, AtomicBoolean> getMapLogMonitoringThread() {
        return asyncLogMonitoringService.getRunningTasks();
    }

    public String createAsyncLogMonitorByQuery(Map<String, String> query) {
        String creatingAsyncTaskResult = NEW_LOG_MONITORING_TASK_ADDED;
        try {
            boolean doesTaskExist = asyncLogMonitoringService.checkQueryExists(query);
            if (!doesTaskExist) {
                asyncLogMonitoringService.monitorLogByQuery(query);
            } else {
                creatingAsyncTaskResult = LOG_MONITORING_TASK_ALREADY_EXISTS;
            }
        } catch (RejectedExecutionException ex) {
            creatingAsyncTaskResult = ex.getMessage();
        }
        return creatingAsyncTaskResult;
    }

    public String cancelLogMonitoringTask(Map<String, String> query) {
        String cancelingAsyncTaskResult = LOG_MONITORING_TASK_DOES_NOT_EXISTS;
        boolean doesTaskExist = asyncLogMonitoringService.checkQueryExists(query);
        if (doesTaskExist) {
            asyncLogMonitoringService.cancelLogMonitoringTask(query);
            cancelingAsyncTaskResult = LOG_MONITORING_TASK_TERMINATED;
        }
        return cancelingAsyncTaskResult;
    }
}
