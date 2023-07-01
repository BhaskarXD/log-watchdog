package spr.graylog.analytics.logwatchdog.util;

import java.util.LinkedList;
import java.util.Queue;

//@Component
//@Scope("prototype")
public class SlidingWindowStatsComputer {
    private final Queue<Long> dataPoints; // Queue to store the previous data points
    private long sum; // Sum of the data points
    private double mean; // Mean of the data points
    private double sumOfSquares; // Sum of squares of the ata points
    private double standardDeviation; // Standard deviation of the data points
    private final long windowSize ; // Size of the sliding window

    public SlidingWindowStatsComputer(long windowSize) {
        this.dataPoints = new LinkedList<>();
        this.sum = 0;
        this.mean = 0.0;
        this.sumOfSquares = 0.0;
        this.standardDeviation = 0.0;
        this.windowSize=windowSize;
    }

    public void addDataPoint(long dataPoint) {
        // Add the new data point to the rear of the queue
        dataPoints.offer(dataPoint);

        // If the queue size exceeds the window size, remove the oldest data point
        if (dataPoints.size() > windowSize) {
            long oldestDataPoint = dataPoints.poll();
            sum -= oldestDataPoint;
            sumOfSquares -= oldestDataPoint * oldestDataPoint;
        }

        // Update the sum and sum of squares
        sum += dataPoint;
        sumOfSquares += dataPoint * dataPoint;

        // Update the mean and standard deviation
        int numDataPoints = dataPoints.size();
        mean = (double) sum / numDataPoints;
        standardDeviation = Math.sqrt((sumOfSquares - numDataPoints * mean * mean) / (numDataPoints - 1));
    }

    public double getMean() {
        return mean;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }
}

