package spr.graylog.analytics.logwatchdog.service;

import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import spr.graylog.analytics.logwatchdog.client.PythonModelClient;
import spr.graylog.analytics.logwatchdog.model.DateHistogramData;
import spr.graylog.analytics.logwatchdog.model.PredictionData;
import spr.graylog.analytics.logwatchdog.repository.ElasticHLRCRepository;
import spr.graylog.analytics.logwatchdog.util.ElasticQueryBuilderUtil;
import spr.graylog.analytics.logwatchdog.util.SlidingWindowStatsComputer;
import spr.graylog.analytics.logwatchdog.util.StartDateTimestamp;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AsyncMlLogMonitoringService {
    private final ElasticHLRCRepository elasticHLRCRepository;
    private final PythonModelClient pythonModelClient;
    private final ConcurrentHashMap<Map<String, String>, AtomicBoolean> runningTasks;
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncMlLogMonitoringService.class);

    public AsyncMlLogMonitoringService(ElasticHLRCRepository elasticHLRCRepository, PythonModelClient pythonModelClient) {
        this.elasticHLRCRepository = elasticHLRCRepository;
        this.pythonModelClient = pythonModelClient;
        this.runningTasks = new ConcurrentHashMap<>();
    }

    @Async("MachineLearningAnomalyDetectionMultiThreadingBean")
    public void processLogMonitoring(Map<String, String> query) {
        AtomicBoolean cancelFlag = new AtomicBoolean(false);
        runningTasks.put(query, cancelFlag);

        BoolQueryBuilder boolQuery = ElasticQueryBuilderUtil.boolQueryBuilderForMapQuery(query);
        LocalDateTime histogramEndTimestamp = StartDateTimestamp.getMlModelStartTimestamp();
        LocalDateTime histogramStartTimestamp = histogramEndTimestamp.minusDays(5);

        try {
            SearchResponse searchResponse = null;
            searchResponse = elasticHLRCRepository.getDateHistogramBetweenTimestamps(boolQuery, histogramStartTimestamp, histogramEndTimestamp);

            List<DateHistogramData> histogramData = parseDateHistogramResponse(searchResponse);
            if (histogramData.isEmpty()) {
                throw new IllegalStateException("Empty histogram data received");
            }

            List<PredictionData> mlModelResponse = pythonModelClient.makePredictionRequest(histogramData);
            if (mlModelResponse.isEmpty()) {
                throw new IllegalStateException("Failed to get prediction data from Python model");
            }

            PredictionData currPredictionData;
            String currDateTimeAsString;
            LocalDateTime currDateTime;
            LocalDateTime aggregationEndTimestamp;
            long aggregationResult;

            for (int i = 0; i < mlModelResponse.size() && !cancelFlag.get(); i++) {
                currPredictionData = mlModelResponse.get(i);
                currDateTimeAsString = currPredictionData.getDs();
                currDateTime = getParsedDateTime(currDateTimeAsString);
                aggregationEndTimestamp = currDateTime.plusMinutes(5);
                TotalHits totalHits = elasticHLRCRepository.getRecordsGeneratedBetweenTimestamps(boolQuery, currDateTime, aggregationEndTimestamp).getHits().getTotalHits();

                if (totalHits == null) {
                    aggregationResult = 0;
                } else {
                    aggregationResult = totalHits.value;
                }

                double yhat = currPredictionData.getYhat();
                double yhatLower = currPredictionData.getYhat_lower();
                double yhatUpper = currPredictionData.getYhat_upper();

                double uncertainty = yhatUpper - yhatLower;
                double error = (aggregationResult - yhat);
                double anomalyScore = (error / uncertainty) * 100;

                if (error > uncertainty) {
                    LOGGER.warn("Anomalous data detected - Timestamp: {}, AggregationResult: {}, Yhat: {}, Uncertainty: {}, AnomalyScore: {}",
                            aggregationEndTimestamp, aggregationResult, yhat, uncertainty, anomalyScore);
                }
                Thread.sleep(100);
            }
        } catch (Exception ex) {
            LOGGER.error("Error occurred while monitoring Ml logs.", ex);
        } finally {
            if (cancelFlag.get()) {
                LOGGER.info("Ml Log monitoring task canceled for query: {}", query);
            } else {
                LOGGER.info("Ml Log monitoring exited for query: {}", query);
            }
            runningTasks.remove(query);
        }
    }

    public List<DateHistogramData> parseDateHistogramResponse(SearchResponse searchResponse) {
        ParsedDateHistogram parsedHistogram = searchResponse.getAggregations().get(elasticHLRCRepository.getDateHistogramName());

        List<DateHistogramData> histogramDataList = new ArrayList<>();
        for (Histogram.Bucket bucket : parsedHistogram.getBuckets()) {
            String dateAsString = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            DateHistogramData histogramData = new DateHistogramData(dateAsString, docCount);
            histogramDataList.add(histogramData);
        }
        return histogramDataList;
    }

    public String prepareJSONRequest(List<DateHistogramData> histogramDataList) {
        JSONArray jsonArray = new JSONArray();

        for (DateHistogramData histogramData : histogramDataList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("dateTime", histogramData.getDate());
            jsonObject.put("docCount", histogramData.getDocCount());
            jsonArray.put(jsonObject);
        }

        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("dataTime", jsonArray);

        return jsonRequest.toString();
    }

    public LocalDateTime getParsedDateTime(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateString, formatter);
    }

    public Boolean checkQueryExists(Map<String, String> query) {
        AtomicBoolean taskBoolean = runningTasks.get(query);
        return taskBoolean != null;
    }

    public void cancelLogMonitoringTask(Map<String, String> query) {
        AtomicBoolean cancelFlag = runningTasks.get(query);
        if (cancelFlag != null) {
            cancelFlag.set(true);
        }
    }

    public ConcurrentMap<Map<String, String>, AtomicBoolean> getRunningTasks() {
        return runningTasks;
    }
}





