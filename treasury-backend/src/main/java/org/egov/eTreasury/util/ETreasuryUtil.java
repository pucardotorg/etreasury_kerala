package org.egov.eTreasury.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Component
@Slf4j
public class ETreasuryUtil {

    private final RestTemplate restTemplate;


    @Autowired
    public ETreasuryUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<?> callService(String inputHeaders, String inputBody, String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.ALL));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("input_headers", inputHeaders);
        body.add("input_data", inputBody);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(url, requestEntity, Object.class);
    }


    public ResponseEntity<?> callAuthService(String clientId, String clientSecret, String payload, String url) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(Collections.singletonList(MediaType.ALL));

        httpHeaders.add("clientId", clientId);
        httpHeaders.add("clientSecret", clientSecret);

        HttpEntity<String> httpEntity = new HttpEntity<>(payload, httpHeaders);
        return restTemplate.postForEntity(url, httpEntity, Object.class);
    }
}
