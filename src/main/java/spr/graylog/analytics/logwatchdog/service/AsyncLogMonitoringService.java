package spr.graylog.analytics.logwatchdog.service;

import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import spr.graylog.analytics.logwatchdog.repository.ElasticHLRCRepository;
import spr.graylog.analytics.logwatchdog.util.SlidingWindowStatsComputer;
import spr.graylog.analytics.logwatchdog.util.StartDateTimestamp;

import java.io.IOException;
import java.time.LocalDateTime;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncLogMonitoringService.class);

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
        LocalDateTime startTimestamp= StartDateTimestamp.getStartTimestamp();

        try {
            List<Long> docCountList=getDateHistogramDocCount(boolQuery,startTimestamp);
            System.out.println(docCountList);
            SlidingWindowStatsComputer slidingWindowStatsComputer= new SlidingWindowStatsComputer(docCountList);
            long curLogsGenerated;
            for(int i=0; i<120 && !cancelFlag.get(); i++){
                LocalDateTime currTimestamp=startTimestamp.plusMinutes(5);
                TotalHits totalHits=elasticHLRCRepository.getRecordsGeneratedBetweenTimestamps(boolQuery,startTimestamp,currTimestamp).getHits().getTotalHits();
                if(totalHits==null){
                    curLogsGenerated=(long)slidingWindowStatsComputer.getMean();
                }else{
                    curLogsGenerated=totalHits.value;
                }
                double zScore=slidingWindowStatsComputer.calculateZScore(curLogsGenerated);
                if (zScore > 3) {
                    LOGGER.warn("Anomalous data detected - Timestamp: {}, LogsGenerated: {}, Z-Score: {}", startTimestamp, curLogsGenerated, zScore);
                }
                slidingWindowStatsComputer.addDataPoint(curLogsGenerated);
                startTimestamp = currTimestamp.plusMinutes(0);
                Thread.sleep(100);
            }
        } catch (Exception ex) {
            LOGGER.error("Error occurred while monitoring logs.", ex);
        }finally {
            if (cancelFlag.get()) {
                LOGGER.info("Log monitoring task canceled for query: {}", query);
            } else {
                LOGGER.info("Log monitoring task completed successfully for query: {}", query);
            }
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

    public ConcurrentMap<Map<String, String>, AtomicBoolean> getRunningTasks() {
        return runningTasks;
    }
}
