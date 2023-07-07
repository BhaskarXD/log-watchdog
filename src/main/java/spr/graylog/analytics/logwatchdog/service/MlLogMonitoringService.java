package spr.graylog.analytics.logwatchdog.service;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import spr.graylog.analytics.logwatchdog.client.PythonModelClient;
import spr.graylog.analytics.logwatchdog.model.DateHistogramData;
import spr.graylog.analytics.logwatchdog.repository.ElasticHLRCRepository;
import spr.graylog.analytics.logwatchdog.util.StartDateTimestamp;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MlLogMonitoringService {
    private final ElasticHLRCRepository elasticHLRCRepository;
    private final ElasticQueryBuilderService elasticQueryBuilderService;
    private final PythonModelClient pythonModelClient;

    public MlLogMonitoringService(ElasticHLRCRepository elasticHLRCRepository, ElasticQueryBuilderService elasticQueryBuilderService, PythonModelClient pythonModelClient) {
        this.elasticHLRCRepository = elasticHLRCRepository;
        this.elasticQueryBuilderService = elasticQueryBuilderService;
        this.pythonModelClient = pythonModelClient;
    }

    public String processLogMonitoring(Map<String, String> filterParams) {
        BoolQueryBuilder boolQuery = elasticQueryBuilderService.boolQueryBuilderForMapQuery(filterParams);
        LocalDateTime histogramEndTimestamp= StartDateTimestamp.getStartTimestamp();
        LocalDateTime histogramStartTimestamp=histogramEndTimestamp.minusHours(3);


        SearchResponse searchResponse = null;
        try {
            searchResponse = elasticHLRCRepository.getDateHistogramBetweenTimestamps(boolQuery,histogramStartTimestamp,histogramEndTimestamp);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }


        List<DateHistogramData> histogramData = parseDateHistogramResponse(searchResponse);
        String mlModelResponse = pythonModelClient.makePredictionRequest(histogramData);

        // Process and return the ML model response as needed
        // String processedResponse = processMLModelResponse(mlModelResponse);

        return mlModelResponse;
    }

    public List<DateHistogramData> parseDateHistogramResponse(SearchResponse searchResponse) {
        ParsedDateHistogram parsedHistogram = searchResponse.getAggregations().get(elasticHLRCRepository.getDateHistogramName());

        List<DateHistogramData> histogramDataList = new ArrayList<>();
        for (Histogram.Bucket bucket : parsedHistogram.getBuckets()) {
            String dateAsString = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            LocalDateTime date = LocalDateTime.parse(dateAsString, DateTimeFormatter.ISO_DATE_TIME);
            DateHistogramData histogramData = new DateHistogramData(date, docCount);
            histogramDataList.add(histogramData);
        }

        return histogramDataList;
    }

    public String prepareJSONRequest(List<DateHistogramData> histogramDataList) {
        JSONArray jsonArray = new JSONArray();

        for (DateHistogramData histogramData : histogramDataList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("dateTime", histogramData.getDate().toString());
            jsonObject.put("docCount", histogramData.getDocCount());
            jsonArray.put(jsonObject);
        }

        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("dataTime", jsonArray);

        return jsonRequest.toString();
    }
}





