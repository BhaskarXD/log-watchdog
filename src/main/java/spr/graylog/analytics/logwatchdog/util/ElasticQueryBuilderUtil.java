package spr.graylog.analytics.logwatchdog.util;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.Map;

public class ElasticQueryBuilderUtil {
    public static BoolQueryBuilder boolQueryBuilderForMapQuery(Map<String, String> filterQueryMap) {
        BoolQueryBuilder boolFilter = QueryBuilders.boolQuery();
        for (Map.Entry<String, String> entry : filterQueryMap.entrySet()) {
            boolFilter.must(QueryBuilders.matchQuery(entry.getKey(), entry.getValue()));
        }
        return boolFilter;
    }

    private ElasticQueryBuilderUtil() {
    }
}
