package org.egov.eTreasury.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Component
@Slf4j
public class RefundUtil {


    private final RestTemplate restTemplate;

    @Autowired
    public RefundUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <T> ResponseEntity<T> callRefundService(String clientId, String authToken, String payload, String url,  Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("clientId", clientId);
        headers.add("authToken", authToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);

        return restTemplate.postForEntity(url, requestEntity, responseType);
    }
}
