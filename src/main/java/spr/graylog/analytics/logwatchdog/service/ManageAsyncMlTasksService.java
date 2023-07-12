package spr.graylog.analytics.logwatchdog.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ManageAsyncMlTasksService {
    private static final String NEW_ML_LOG_MONITORING_TASK_ADDED = "NEW_ML_LOG_MONITORING_TASK_ADDED";
    private static final String ML_LOG_MONITORING_TASK_ALREADY_EXISTS = "ML_LOG_MONITORING_TASK_ALREADY_EXISTS";
    private static final String ML_LOG_MONITORING_TASK_DOES_NOT_EXISTS = "ML_LOG_MONITORING_TASK_DOES_NOT_EXISTS";
    private static final String ML_LOG_MONITORING_TASK_TERMINATED = "ML_LOG_MONITORING_TASK_TERMINATED";
    private final AsyncMlLogMonitoringService asyncMlLogMonitoringService;

    public ManageAsyncMlTasksService(AsyncMlLogMonitoringService asyncMlLogMonitoringService) {
        this.asyncMlLogMonitoringService = asyncMlLogMonitoringService;
    }

    public Map<Map<String, String>, AtomicBoolean> getMapMlLogMonitoringThread() {
        return asyncMlLogMonitoringService.getRunningTasks();
    }

    public String createAsyncMlLogMonitorByQuery(Map<String, String> query) {
        String creatingAsyncTaskResult = NEW_ML_LOG_MONITORING_TASK_ADDED;
        try {
            boolean doesTaskExist = asyncMlLogMonitoringService.checkQueryExists(query);
            if (!doesTaskExist) {
                asyncMlLogMonitoringService.processLogMonitoring(query);
            } else {
                creatingAsyncTaskResult = ML_LOG_MONITORING_TASK_ALREADY_EXISTS;
            }
        } catch (RejectedExecutionException ex) {
            creatingAsyncTaskResult = ex.getMessage();
        }
        return creatingAsyncTaskResult;
    }

    public String cancelMlLogMonitoringTask(Map<String, String> query) {
        String cancelingAsyncTaskResult = ML_LOG_MONITORING_TASK_DOES_NOT_EXISTS;
        boolean doesTaskExist = asyncMlLogMonitoringService.checkQueryExists(query);
        if (doesTaskExist) {
            asyncMlLogMonitoringService.cancelLogMonitoringTask(query);
            cancelingAsyncTaskResult = ML_LOG_MONITORING_TASK_TERMINATED;
        }
        return cancelingAsyncTaskResult;
    }
}
