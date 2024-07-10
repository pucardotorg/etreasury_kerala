package org.egov.eTreasury.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class ConnectionUtil {

    private final RestTemplate restTemplate;

    @Autowired
    public ConnectionUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <T> ResponseEntity<T> callService(String url, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        List<MediaType> mediaTypeList = new ArrayList<>();
        mediaTypeList.add(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypeList);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(null, headers);
        return restTemplate.postForEntity(url, requestEntity, responseType);
    }
}
