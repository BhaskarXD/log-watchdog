package spr.graylog.analytics.logwatchdog.config;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestHighLevelClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchRestHighLevelClientConfig {
    private final RestClient restClient;
    @Autowired
    public ElasticsearchRestHighLevelClientConfig(RestClient restClient) {
        this.restClient = restClient;
    }
    @Bean
    public RestHighLevelClient getElasticsearchHLRC(){
        // Create the HLRC
        return new RestHighLevelClientBuilder(restClient)
                .setApiCompatibilityMode(true)
                .build();
    }
}
