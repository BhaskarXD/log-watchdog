package spr.graylog.analytics.logwatchdog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spr.graylog.analytics.logwatchdog.service.ElasticApiService;
import spr.graylog.analytics.logwatchdog.service.ManageAsyncTasksService;
import spr.graylog.analytics.logwatchdog.service.MlLogMonitoringService;
import spr.graylog.analytics.logwatchdog.service.TempTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/watchdog")
public class ElasticApiController {
    private final ElasticApiService elasticApiService;
    private final ManageAsyncTasksService manageAsyncTasksService;
    private final MlLogMonitoringService mlLogMonitoringService;
    private final TempTest tempTest;

    public ElasticApiController(ElasticApiService elasticApiService, ManageAsyncTasksService manageAsyncTasksService, MlLogMonitoringService mlLogMonitoringService, TempTest tempTest) {
        this.elasticApiService = elasticApiService;
        this.manageAsyncTasksService = manageAsyncTasksService;
        this.mlLogMonitoringService = mlLogMonitoringService;
        this.tempTest = tempTest;
    }

    @Autowired


    //-------------------------- methods that interact with the api ----------------------------------------------------

    @GetMapping("/anomalies/moments")
    public ResponseEntity<List<String>> detectAnomaliesByTimestamp(
            @RequestParam("source") String source,
            @RequestParam("timestamp") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp) throws IOException {

        List<String> anomalies = elasticApiService.detectAnomaliesAroundTimestamp(source, timestamp);
        return ResponseEntity.ok(anomalies);
    }

    @GetMapping("/anomalies/test")
    public void detectAnomaliesByTimestamp() {
        tempTest.doSomething();
    }

    @PostMapping("/anomalies/async")
    public String addtask(@RequestBody Map<String, String> query) {
        return manageAsyncTasksService.createAsyncLogMonitorByQuery(query);
    }
    @GetMapping("/anomalies/futures")
    public Map<Map<String,String>, AtomicBoolean> gettasks(){
        return manageAsyncTasksService.getMapLogMonitoringThread();
    }

    @DeleteMapping("anomalies/tasks")
    public String deletetask(@RequestBody Map<String, String> query){
        return manageAsyncTasksService.cancelLogMonitoringTask(query);
    }

    //----------- method to interact with python ml model ---------------------

    @PostMapping("/anomalies/ml")
    public ResponseEntity<String> monitorLogs(@RequestBody Map<String, String> filterParams) {
        try {
            String response = mlLogMonitoringService.processLogMonitoring(filterParams);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }

}


