package spr.graylog.analytics.logwatchdog.util;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Component;

@Component
public class BoolQueryBuilderUtil {
    public BoolQueryBuilder deepCopyBoolQuery(BoolQueryBuilder originalQuery) {
        BoolQueryBuilder newQuery = new BoolQueryBuilder();
        copyClauses(originalQuery, newQuery);
        return newQuery;
    }

    private void copyClauses(BoolQueryBuilder source, BoolQueryBuilder destination) {
        for (QueryBuilder mustClause : source.must()) {
            destination.must(mustClause);
        }

        for (QueryBuilder mustNotClause : source.mustNot()) {
            destination.mustNot(mustNotClause);
        }

        for (QueryBuilder shouldClause : source.should()) {
            destination.should(shouldClause);
        }

        destination.minimumShouldMatch(source.minimumShouldMatch());
    }
}
