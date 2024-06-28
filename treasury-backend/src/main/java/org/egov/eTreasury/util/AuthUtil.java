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
public class AuthUtil {

    private final RestTemplate restTemplate;

    @Autowired
    public AuthUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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
