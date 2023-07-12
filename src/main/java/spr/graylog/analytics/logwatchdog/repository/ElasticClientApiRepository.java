package spr.graylog.analytics.logwatchdog.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.FieldDateMath;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDateTime;

@Repository
public class ElasticClientApiRepository {
    @Value("${elasticsearch.index}")
    private String esIndex;
    private final ElasticsearchClient elasticsearchClient;

    public ElasticClientApiRepository(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }


    public SearchResponse<Void> getTimeHistogramPipelineExtendedStats(String source, LocalDateTime startTimestamp, LocalDateTime endTimestamp) throws IOException {
        Query query = MatchQuery.of(q -> q.field("source").query(source))._toQuery();
        return elasticsearchClient.search(b0 -> b0
                        .index(esIndex)
                        .query(query)
                        .aggregations("logs_per_minute", a -> a
                                .dateHistogram(d -> d
                                        .field("csv_timestamp")
                                        .fixedInterval(t -> t
                                                .time("1m")).minDocCount(0).extendedBounds(r -> r
                                                .min(FieldDateMath.of(l -> l
                                                        .expr(startTimestamp.toString()))).
                                                max(FieldDateMath.of(l -> l
                                                        .expr(endTimestamp.toString()))
                                                )
                                        )
                                )
                        ).aggregations("doc_count_stats", b1 -> b1.extendedStatsBucket(b2 -> b2.bucketsPath(b3 -> b3.single("logs_per_minute>_count"))))
                ,
                Void.class);
    }
}
