package spr.graylog.analytics.logwatchdog.service;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ElasticQueryBuilderService {
    public BoolQueryBuilder boolQueryBuilderForMapQuery(Map<String,String> filterQueryMap){
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        for (Map.Entry<String, String> entry : filterQueryMap.entrySet()) {
            boolQuery.filter(QueryBuilders.matchQuery(entry.getKey(), entry.getValue()));
        }
        return boolQuery;
    }
}
