package com.tananushka.resource.svc.controller;

import com.tananushka.resource.svc.dto.ResourceResponse;
import com.tananushka.resource.svc.service.ResourceService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/resources")
@AllArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping
    public ResponseEntity<ResourceResponse> uploadResource(@RequestBody byte[] audioData) {
        ResourceResponse response = resourceService.saveResource(audioData);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getResource(@PathVariable Integer id) {
        byte[] audioData = resourceService.getResourceData(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("audio/mpeg"));
        return ResponseEntity.ok().headers(headers).body(audioData);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Long>>> deleteResource(@RequestParam String id) {
        List<Long> deletedIds = resourceService.deleteResources(id);
        Map<String, List<Long>> response = Collections.singletonMap("ids", deletedIds);
        return ResponseEntity.ok(response);
    }
}
