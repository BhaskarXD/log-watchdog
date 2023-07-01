package spr.graylog.analytics.logwatchdog.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AsyncLogMonitoringService {
    private final ConcurrentHashMap<Map<String, String>, AtomicBoolean> runningTasks;

    public AsyncLogMonitoringService() {
        this.runningTasks = new ConcurrentHashMap<>();
    }

    @Async
    public CompletableFuture<Void> monitorLogByQuery(Map<String,String> query){


        AtomicBoolean cancelFlag = new AtomicBoolean(false);
        runningTasks.put(query, cancelFlag);
        System.out.println("New thread created");

        for (int i = 0; i < 60 ; i++) {
            // Your asynchronous log monitoring and anomaly detection logic here
            // For example, process log data and detect anomalies

            // Check for cancellation request from another thread
            System.out.println("Inside thread number: " + i);
            System.out.println(Thread.currentThread().getName() + " " + Thread.currentThread().toString());

            if (cancelFlag.get()) {
                // Set the cancelFlag to true to gracefully terminate the task
                System.out.println("Cancellation request received");
                break;
            } else {
                System.out.println("Cancellation request not received");
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // Continue processing the log data
        }

        runningTasks.remove(query);
        return CompletableFuture.completedFuture(null);
    }

    public void cancelLogMonitoringTask(Map<String, String> query) {
        AtomicBoolean cancelFlag = runningTasks.get(query);
        if (cancelFlag != null) {
            cancelFlag.set(true);
        }
    }
}
