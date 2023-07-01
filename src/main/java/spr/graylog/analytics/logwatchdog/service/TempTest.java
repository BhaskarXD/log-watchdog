package spr.graylog.analytics.logwatchdog.service;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
@Service
public class TempTest {
    private final RestHighLevelClient restHighLevelClient;
    @Autowired
    public TempTest(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    public void doSomething(){
        SearchRequest searchRequest = new SearchRequest("rts_week_log");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchRequest.source(searchSourceBuilder);
        try {
            // Execute the search query and get the response
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            for(SearchHit hit: searchResponse.getHits().getHits()){
                System.out.println(hit.getSourceAsString());
            }
            // Handle the searchResponse here (e.g., iterate through hits and process the documents)
            // searchResponse.getHits().forEach(hit -> {
            //     String documentId = hit.getId();
            //     Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //     // Process the document data here
            // });

            // Close the client connection when done
            restHighLevelClient.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
