package spr.graylog.analytics.logwatchdog.service;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ElasticQueryBuilderService {
    public BoolQueryBuilder boolQueryBuilderForMapQuery(Map<String,String> filterQueryMap){
        BoolQueryBuilder boolFilter = QueryBuilders.boolQuery();
        for (Map.Entry<String, String> entry : filterQueryMap.entrySet()) {
            boolFilter.must(QueryBuilders.matchQuery(entry.getKey(), entry.getValue()));
        }
        return boolFilter;
    }
}
