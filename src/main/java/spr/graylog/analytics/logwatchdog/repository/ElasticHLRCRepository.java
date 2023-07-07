package spr.graylog.analytics.logwatchdog.repository;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDateTime;

@Repository
public class ElasticHLRCRepository {
    @Value("${elasticsearch.index}")
    private String elasticsearchIndex;
    @Value("${elasticsearch.timestamp.field}")
    private String elasticsearchTimestampField;
    private static final String DATE_HISTOGRAM_NAME="log_date_histogram";
    private static final int SEARCH_QUERY_RESULT_SIZE=0;
    private final RestHighLevelClient restHighLevelClient;

    public ElasticHLRCRepository(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    public String getDateHistogramName() {
        return DATE_HISTOGRAM_NAME;
    }

    public SearchResponse getDateHistogramBetweenTimestamps(BoolQueryBuilder originalBoolQuery,
                                                            LocalDateTime startTimestamp,
                                                            LocalDateTime endTimestamp) throws IOException {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery().filter(originalBoolQuery);
        boolQuery.filter(QueryBuilders.rangeQuery(elasticsearchTimestampField)
                .gte(startTimestamp)
                .lt(endTimestamp));

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQuery);
        sourceBuilder.size(SEARCH_QUERY_RESULT_SIZE);
        sourceBuilder.aggregation(AggregationBuilders
                .dateHistogram(DATE_HISTOGRAM_NAME)
                .field(elasticsearchTimestampField)
                .fixedInterval(DateHistogramInterval.minutes(5))
                .minDocCount(0));

        SearchRequest searchRequest = new SearchRequest(elasticsearchIndex);
        searchRequest.source(sourceBuilder);

        return restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);
    }
    public SearchResponse getRecordsGeneratedBetweenTimestamps(BoolQueryBuilder originalBoolQuery,
                                                               LocalDateTime startTimestamp,
                                                               LocalDateTime endTimestamp) throws IOException {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery().filter(originalBoolQuery);
        boolQuery.filter(QueryBuilders.rangeQuery(elasticsearchTimestampField)
                .gte(startTimestamp)
                .lt(endTimestamp));

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(SEARCH_QUERY_RESULT_SIZE);
        sourceBuilder.query(boolQuery);
        sourceBuilder.trackTotalHits(true);

        SearchRequest searchRequest = new SearchRequest(elasticsearchIndex);
        searchRequest.source(sourceBuilder);

        return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    }

}
