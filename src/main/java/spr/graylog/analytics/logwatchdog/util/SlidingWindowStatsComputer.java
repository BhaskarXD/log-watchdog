package spr.graylog.analytics.logwatchdog.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SlidingWindowStatsComputer {
    private final Queue<Long> dataPoints;
    private long sum;
    private double mean;
    private double sumOfSquares;
    private double standardDeviation;
    private long windowSize;
    private static final int MINIMUM_WINDOW_SIZE = 6;

    public SlidingWindowStatsComputer(List<Long> initialValues) {
        if (initialValues.size() < MINIMUM_WINDOW_SIZE) {
            throw new IllegalArgumentException("The number of data points must be greater than: " + MINIMUM_WINDOW_SIZE);
        }

        this.dataPoints = new LinkedList<>(initialValues);
        this.windowSize = initialValues.size();
        this.sum = initialValues.stream().mapToLong(Long::longValue).sum();
        this.mean = (double) sum / windowSize;
        this.sumOfSquares = initialValues.stream().mapToDouble(val -> val * val).sum();
        this.standardDeviation = computeStandardDeviation();
    }

    public void addDataPoint(long dataPoint) {
        dataPoints.offer(dataPoint);
        System.out.println("added data point : "+dataPoint);
        if (dataPoints.size() > windowSize) {
            long oldestDataPoint = dataPoints.poll();
            System.out.println("remmoved : "+oldestDataPoint);
            sum -= oldestDataPoint;
            sumOfSquares -= oldestDataPoint * oldestDataPoint;
        }

        sum += dataPoint;
        sumOfSquares += dataPoint * dataPoint;

        int numDataPoints = dataPoints.size();
        mean = (double) sum / numDataPoints;
        standardDeviation = computeStandardDeviation();
    }

    private double computeStandardDeviation() {
        int numDataPoints = dataPoints.size();
        if (numDataPoints <= 1) {
            return 0.0;
        }
        return Math.sqrt((sumOfSquares - numDataPoints * mean * mean) / (numDataPoints - 1));
    }

    public double getMean() {
        return mean;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public double calculateZScore(long dataPoint) {
        if (standardDeviation == 0.0) {
            return (dataPoint > mean) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }
        return (dataPoint - mean) / standardDeviation;
    }
}

