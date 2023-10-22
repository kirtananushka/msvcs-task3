package com.tananushka.resource.svc.client;

import com.tananushka.resource.svc.dto.SongIdResponse;
import com.tananushka.resource.svc.dto.SongRequest;
import com.tananushka.resource.svc.exception.ResourceServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SongClient {

    private final DiscoveryClient discoveryClient;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${svc.song.instance}")
    private String songSvcInstance;

    @Value("${svc.song.songs-endpoint}")
    private String songsEndpoint;

    public SongIdResponse saveMetadata(SongRequest songRequest) {
        List<ServiceInstance> instances = getInstances();

        if (instances.isEmpty()) {
            throw new ResourceServiceException("No instances of Song Service found", "404");
        }

        for (ServiceInstance instance : instances) {
            String endpointUrl = instance.getUri() + songsEndpoint;
            try {
                log.debug("Calling {} with {}", endpointUrl, songRequest);
                return restTemplate.postForObject(endpointUrl, songRequest, SongIdResponse.class);
            } catch (Exception e) {
                log.error("Unexpected error while posting to {}: {}", endpointUrl, e.getMessage());
            }
        }
        throw new ResourceServiceException("Failed to process the request with all available Song Service instances",
              "500");
    }

    public void deleteAll() {
        List<ServiceInstance> instances = getInstances();
        instances.forEach(this::deleteAllSongs);
    }

    private void deleteAllSongs(ServiceInstance instance) throws ResourceServiceException {
        String endpointUrl = instance.getUri() + songsEndpoint + "/all";
        try {
            restTemplate.delete(endpointUrl);
        } catch (Exception e) {
            throw new ResourceServiceException("Failed to delete all songs from: " + endpointUrl, "500");
        }
    }

    private List<ServiceInstance> getInstances() {
        return discoveryClient.getInstances(songSvcInstance);
    }
}
