package spr.graylog.analytics.logwatchdog.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import spr.graylog.analytics.logwatchdog.model.DateHistogramData;
import spr.graylog.analytics.logwatchdog.model.PredictionData;

import java.util.List;

@Service
public class PythonModelClient {
    @Value("${flask.model.url}")
    private String modelUrl;
    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(PythonModelClient.class);


    public PythonModelClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<PredictionData> makePredictionRequest(List<DateHistogramData> histogramDataList) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<DateHistogramData>> requestEntity = new HttpEntity<>(histogramDataList, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(modelUrl, HttpMethod.POST, requestEntity, String.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String response = responseEntity.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(response, new TypeReference<List<PredictionData>>() {
                });
            } catch (JsonProcessingException e) {
                logger.error("Error: Failed to parse JSON response", e);
            }
        } else {
            logger.error("Request failed with status: {}", responseEntity.getStatusCode());

        }
        return Collections.emptyList();
    }
}
