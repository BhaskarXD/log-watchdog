package spr.graylog.analytics.logwatchdog.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import spr.graylog.analytics.logwatchdog.model.DateHistogramData;

import java.util.List;

@Service
public class PythonModelClient {
    @Value("${flask.model.url}")
    private String modelUrl;
    private final RestTemplate restTemplate;

    public PythonModelClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String makePredictionRequest(List<DateHistogramData> histogramDataList) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<DateHistogramData>> requestEntity = new HttpEntity<>(histogramDataList, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(modelUrl, HttpMethod.POST, requestEntity, String.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        } else {
            // Handle error response
            System.out.println("Request failed with status: " + responseEntity.getStatusCode());
            return null;
        }
    }
}
