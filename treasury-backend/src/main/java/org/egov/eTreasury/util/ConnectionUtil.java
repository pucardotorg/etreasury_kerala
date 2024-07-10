package org.egov.eTreasury.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class ConnectionUtil {

    private final RestTemplate restTemplate;

    @Autowired
    public ConnectionUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <T> ResponseEntity<T> callService(String url, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();

        List<MediaType> mediaTypeList = new ArrayList<>();
        mediaTypeList.add(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypeList);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        return restTemplate.postForEntity(url, requestEntity, responseType);
    }
}
