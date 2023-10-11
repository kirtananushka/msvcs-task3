package com.tananushka.resource.svc.client;

import com.tananushka.resource.svc.dto.SongIdResponse;
import com.tananushka.resource.svc.dto.SongRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SongClient {

    private final RestTemplate restTemplate;

    @Value("${svc.song.base-url}")
    private String baseUrl;

    @Value("${svc.song.songs-endpoint}")
    private String songsEndpoint;

    public SongClient() {
        this.restTemplate = new RestTemplate();
    }

    public SongIdResponse postMetadata(SongRequest songRequest) {
        String endpointUrl = baseUrl + songsEndpoint;
        return restTemplate.postForObject(endpointUrl, songRequest, SongIdResponse.class);
    }
}
