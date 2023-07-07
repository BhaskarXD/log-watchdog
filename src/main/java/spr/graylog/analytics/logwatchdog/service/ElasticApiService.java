package spr.graylog.analytics.logwatchdog.service;

import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spr.graylog.analytics.logwatchdog.repository.ElasticClientApiRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ElasticApiService {
    private final ElasticClientApiRepository elasticClientApiRepository;
    @Autowired
    public ElasticApiService(ElasticClientApiRepository elasticClientApiRepository) {
        this.elasticClientApiRepository = elasticClientApiRepository;
    }

    public List<String> detectAnomaliesAroundTimestamp(String source, LocalDateTime timestamp) throws IOException {
        List<String> anomalies = new ArrayList<>();

        // Compute the timestamp range for previous days (adjust the number of days as needed)
        LocalDateTime endTimestamp = timestamp.plusHours(1);
        LocalDateTime startTimestamp = endTimestamp.minusHours(24);

        // Compute the interval in which to detect anomaly based on mean and std value of previous days
        LocalDateTime anomalyDetectionStartTimestamp = timestamp.minusHours(1);


        SearchResponse<Void> response = elasticClientApiRepository.getTimeHistogramPipelineExtendedStats(source,startTimestamp,endTimestamp);

        double avg = response.aggregations().get("doc_count_stats").extendedStatsBucket().avg();
        double stdDeviation = response.aggregations().get("doc_count_stats").extendedStatsBucket().stdDeviation();
        double anomalyUpperLimit=avg+3*stdDeviation;
        double anomalyLowerLimit=avg-3*stdDeviation;
        List<DateHistogramBucket> dateHistogramBuckets=response.aggregations().get("logs_per_minute").dateHistogram().buckets().array();
        long logCount;
        int consecutiveAnomalyCounter=0;
        for(int i = dateHistogramBuckets.size()-1; i>=0 && Objects.requireNonNull(dateHistogramBuckets.get(i).keyAsString()).compareTo(anomalyDetectionStartTimestamp.toString())>0; i--){
            DateHistogramBucket currentHistogramBucket=dateHistogramBuckets.get(i);
            logCount= currentHistogramBucket.docCount();
            if(logCount>anomalyUpperLimit || logCount<anomalyLowerLimit){
                consecutiveAnomalyCounter++;
            }else if (consecutiveAnomalyCounter>0){
                consecutiveAnomalyCounter=0;
            }
            if(consecutiveAnomalyCounter>4) {
                anomalies.add(currentHistogramBucket.toString());
                consecutiveAnomalyCounter = 0;
            }
        }

        return anomalies;
    }
}
