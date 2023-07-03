package spr.graylog.analytics.logwatchdog.service;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import spr.graylog.analytics.logwatchdog.repository.ElasticHLRCRepository;
import spr.graylog.analytics.logwatchdog.util.SlidingWindowStatsComputer;

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
public class AsyncLogMonitoringService {
    private final ConcurrentHashMap<Map<String, String>, AtomicBoolean> runningTasks;
    private final ElasticHLRCRepository elasticHLRCRepository;
    private final ElasticQueryBuilderService elasticQueryBuilderService;

    public AsyncLogMonitoringService(ElasticHLRCRepository elasticHLRCRepository, ElasticQueryBuilderService elasticQueryBuilderService) {
        this.elasticHLRCRepository = elasticHLRCRepository;
        this.elasticQueryBuilderService = elasticQueryBuilderService;
        this.runningTasks = new ConcurrentHashMap<>();
    }

    @Async("AnomalyDetectionMultiThreadingBean")
    public void monitorLogByQuery(Map<String,String> query){
        AtomicBoolean cancelFlag = new AtomicBoolean(false);
        runningTasks.put(query, cancelFlag);

        BoolQueryBuilder boolQuery=elasticQueryBuilderService.boolQueryBuilderForMapQuery(query);
        LocalDateTime startTimestamp=getStartTimestamp();

        try {
            List<Long> docCountList=getDateHistogramDocCount(boolQuery,startTimestamp);
            SlidingWindowStatsComputer slidingWindowStatsComputer= new SlidingWindowStatsComputer(docCountList);
            for(int i=0; i<36; i++){
                LocalDateTime currTimestamp=startTimestamp.plusMinutes(5);

                long curLogsGenerated=elasticHLRCRepository.getRecordsGeneratedBetweenTimestamps(boolQuery,startTimestamp,currTimestamp).getHits().getTotalHits().value;
                System.out.println("z score : "+slidingWindowStatsComputer.calculateZScore(curLogsGenerated));
                slidingWindowStatsComputer.addDataPoint(curLogsGenerated);
                startTimestamp = currTimestamp.plusMinutes(0);
                Thread.sleep(1000);
            }

        } catch (Exception ex) {
            System.out.println("isndie catch"+ex.toString());

            throw new RuntimeException(ex);
        }finally {
            runningTasks.remove(query);
        }
    }
    public List<Long> getDateHistogramDocCount(BoolQueryBuilder boolQuery, LocalDateTime histogramEndTimestamp) throws IOException {
        LocalDateTime histogramStartTimestamp=histogramEndTimestamp.minusHours(3);
        SearchResponse histogramSearchResponse=elasticHLRCRepository
                .getDateHistogramBetweenTimestamps(
                        boolQuery,
                        histogramStartTimestamp,
                        histogramEndTimestamp);
        ParsedDateHistogram parsedDateHistogram=histogramSearchResponse
                .getAggregations()
                .get(elasticHLRCRepository.getDateHistogramName());

        List<Long> docCounts = new ArrayList<>();
        for (Histogram.Bucket bucket : parsedDateHistogram.getBuckets()) {
            docCounts.add(bucket.getDocCount());
        }
        return docCounts;
    }

    public Boolean checkQueryExists(Map<String,String> query){
        AtomicBoolean taskBoolean = runningTasks.get(query);
        return taskBoolean != null;
    }

    public void cancelLogMonitoringTask(Map<String, String> query) {
        AtomicBoolean cancelFlag = runningTasks.get(query);
        if (cancelFlag != null) {
            cancelFlag.set(true);
        }
    }

    private LocalDateTime getStartTimestamp(){
        String dateString = "2023-06-26T00:00:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return LocalDateTime.parse(dateString, formatter);
    }

    public ConcurrentMap<Map<String, String>, AtomicBoolean> getRunningTasks() {
        return runningTasks;
    }
}
