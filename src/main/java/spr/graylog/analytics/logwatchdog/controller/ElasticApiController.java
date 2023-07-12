package spr.graylog.analytics.logwatchdog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spr.graylog.analytics.logwatchdog.service.ElasticApiService;
import spr.graylog.analytics.logwatchdog.service.ManageAsyncMlTasksService;
import spr.graylog.analytics.logwatchdog.service.ManageAsyncTasksService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/watchdog/anomalies")
public class ElasticApiController {
    private final ElasticApiService elasticApiService;
    private final ManageAsyncTasksService manageAsyncTasksService;
    private final ManageAsyncMlTasksService manageAsyncMlTasksService;

    @Autowired
    public ElasticApiController(ElasticApiService elasticApiService,
                                ManageAsyncTasksService manageAsyncTasksService,
                                ManageAsyncMlTasksService manageAsyncMlTasksService) {
        this.elasticApiService = elasticApiService;
        this.manageAsyncTasksService = manageAsyncTasksService;
        this.manageAsyncMlTasksService = manageAsyncMlTasksService;
    }

    //-------------------------- methods that interact with the api ----------------------------------------------------

    @GetMapping("/moments")
    public ResponseEntity<List<String>> detectAnomaliesByTimestamp(
            @RequestParam("source") String source,
            @RequestParam("timestamp") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp) throws IOException {

        List<String> anomalies = elasticApiService.detectAnomaliesAroundTimestamp(source, timestamp);
        return ResponseEntity.ok(anomalies);
    }

    @PostMapping("/async")
    public String addTask(@RequestBody Map<String, String> query) {
        return manageAsyncTasksService.createAsyncLogMonitorByQuery(query);
    }

    @GetMapping("/futures")
    public Map<Map<String, String>, AtomicBoolean> getTasks() {
        return manageAsyncTasksService.getMapLogMonitoringThread();
    }

    @DeleteMapping("/tasks")
    public String deleteTask(@RequestBody Map<String, String> query) {
        return manageAsyncTasksService.cancelLogMonitoringTask(query);
    }

    //----------- method to interact with python ml model ---------------------

    @PostMapping("/ml")
    public String monitorLogs(@RequestBody Map<String, String> query) {
        return manageAsyncMlTasksService.createAsyncMlLogMonitorByQuery(query);
    }

    @GetMapping("/mlFutures")
    public Map<Map<String, String>, AtomicBoolean> getMlTasks() {
        return manageAsyncMlTasksService.getMapMlLogMonitoringThread();
    }

    @DeleteMapping("/mlTasks")
    public String deleteMlTask(@RequestBody Map<String, String> query) {
        return manageAsyncMlTasksService.cancelMlLogMonitoringTask(query);
    }
}


