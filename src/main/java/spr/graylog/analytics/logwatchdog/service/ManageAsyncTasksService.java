package spr.graylog.analytics.logwatchdog.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;

@Service
public class ManageAsyncTasksService {
    private static final  String NEW_LOG_MONITORING_TASK_ADDED="NEW_LOG_MONITORING_TASK_ADDED";
    private static final  String LOG_MONITORING_TASK_ALREADY_EXISTS="LOG_MONITORING_TASK_ALREADY_EXISTS";
    private static final  String LOG_MONITORING_TASK_DOES_NOT_EXISTS="LOG_MONITORING_TASK_DOES_NOT_EXISTS";
    private static final  String LOG_MONITORING_TASK_ALREADY_DONE="LOG_MONITORING_TASK_ALREADY_DONE";
    private static final  String LOG_MONITORING_TASK_TERMINATED="LOG_MONITORING_TASK_TERMINATED";

    private final AsyncLogMonitoringService asyncLogMonitoringService;
    private final Map<Map<String,String>, CompletableFuture<Void>> mapLogMonitoringThreadFutures;

    public ManageAsyncTasksService(AsyncLogMonitoringService asyncLogMonitoringService) {
        this.asyncLogMonitoringService = asyncLogMonitoringService;
        this.mapLogMonitoringThreadFutures = new HashMap<>();
    }

    public Map<Map<String,String>, CompletableFuture<Void>> getMapLogMonitoringThreadFutures() {
        return mapLogMonitoringThreadFutures;
    }

    public String  createAsyncLogMonitorByQuery(Map<String ,String> query) {
        String creatingAsyncTaskResult=NEW_LOG_MONITORING_TASK_ADDED;
        try {
            if (!mapLogMonitoringThreadFutures.containsKey(query)) {
                CompletableFuture<Void> anomalyDetectorTaskFuture = asyncLogMonitoringService.monitorLogByQuery(query);
                mapLogMonitoringThreadFutures.put(query, anomalyDetectorTaskFuture);
            } else {
                creatingAsyncTaskResult = LOG_MONITORING_TASK_ALREADY_EXISTS;
            }
        } catch (RejectedExecutionException ex) {
            creatingAsyncTaskResult = ex.getMessage();
        }
        return creatingAsyncTaskResult;
    }
    public String cancelLogMonitoringTask(Map<String, String> query) {
        String cancelingAsyncTaskResult=LOG_MONITORING_TASK_DOES_NOT_EXISTS;
        CompletableFuture<Void> taskFuture = mapLogMonitoringThreadFutures.get(query);
        if (taskFuture != null) {
            if(!taskFuture.isDone()){
                asyncLogMonitoringService.cancelLogMonitoringTask(query);
                cancelingAsyncTaskResult=LOG_MONITORING_TASK_TERMINATED;

            }else{
                cancelingAsyncTaskResult=LOG_MONITORING_TASK_ALREADY_DONE;
            }
        }
        return cancelingAsyncTaskResult;
    }
}
