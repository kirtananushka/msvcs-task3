package com.tananushka.resource.svc.controller;

import com.tananushka.resource.svc.client.SongClient;
import com.tananushka.resource.svc.dto.ResourceResponse;
import com.tananushka.resource.svc.dto.SongRequest;
import com.tananushka.resource.svc.dto.SongIdResponse;
import com.tananushka.resource.svc.entity.Resource;
import com.tananushka.resource.svc.repository.ResourceRepository;
import com.tananushka.resource.svc.service.ResourceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResourceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceService resourceService;

    @MockBean
    private ResourceRepository resourceRepository;

    @MockBean
    private SongClient songClient;

    @Test
    void testUploadResource_WhenValidAudio_ThenReturnOk() throws Exception {
        byte[] audioData = Files.readAllBytes(Path.of("src/test/resources/valid-audio-data.mp3"));

        Resource resourceToSave = new Resource();
        resourceToSave.setAudioData(audioData);
        Resource savedResource = new Resource();
        savedResource.setId(1L);
        savedResource.setAudioData(audioData);
        when(resourceRepository.save(any(Resource.class))).thenReturn(savedResource);

        SongIdResponse songIdResponse = new SongIdResponse();
        songIdResponse.setId(1);
        when(songClient.postMetadata(any(SongRequest.class))).thenReturn(songIdResponse);

        ResourceResponse response = resourceService.saveResource(audioData);

        mockMvc.perform(post("/resources").contentType(MediaType.APPLICATION_OCTET_STREAM).content(audioData))
              .andExpect(status().isOk()).andReturn();
        assertThat(response.getId(), equalTo(1));
    }

    @Test
    void testUploadResource_WhenInvalidAudio_ThenReturnBadRequestStatus400() throws Exception {
        byte[] invalidAudioData = Files.readAllBytes(Path.of("src/test/resources/invalid-audio-data.mp3"));

        mockMvc.perform(post("/resources").contentType(MediaType.APPLICATION_OCTET_STREAM).content(invalidAudioData))
              .andExpect(status().isBadRequest());
    }

    @Test
    void testUploadResource_WhenUnexpectedError_ThenReturnInternalServerStatus500() throws Exception {
        byte[] audioData = Files.readAllBytes(Path.of("src/test/resources/valid-audio-data.mp3"));

        doThrow(new RuntimeException("Unexpected error")).when(resourceRepository).save(any());

        mockMvc.perform(post("/resources").contentType(MediaType.APPLICATION_JSON).content(audioData))
              .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetResource_WhenValidResourceId_ThenReturnAudioData() throws Exception {
        Long resourceId = 1L;
        byte[] expectedAudioData = Files.readAllBytes(Path.of("src/test/resources/valid-audio-data.mp3"));

        Resource retrievedResource = new Resource();
        retrievedResource.setId(resourceId);
        retrievedResource.setAudioData(expectedAudioData);
        when(resourceRepository.existsById(anyLong())).thenReturn(true);
        when(resourceRepository.findById(anyLong())).thenReturn(Optional.of(retrievedResource));

        mockMvc.perform(get("/resources/" + resourceId).accept(MediaType.APPLICATION_OCTET_STREAM))
              .andExpect(status().isOk()).andExpect(result -> {
                  byte[] returnedAudioData = result.getResponse().getContentAsByteArray();
                  assertThat(returnedAudioData, equalTo(expectedAudioData));
              });
    }

    @Test
    void testGetResource_WhenInvalidResourceId_ThenReturnNotFoundStatus404() throws Exception {
        Long invalidResourceId = 999L;
        when(resourceRepository.findById(invalidResourceId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/resources/" + invalidResourceId).accept(MediaType.APPLICATION_JSON))
              .andExpect(status().isNotFound());
    }

    @Test
    void testGetResource_WhenUnexpectedError_ThenReturnInternalServerStatus500() throws Exception {
        long resourceId = 1L;

        when(resourceRepository.existsById(anyLong())).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(resourceRepository).findById(anyLong());

        mockMvc.perform(get("/resources/" + resourceId).accept(MediaType.APPLICATION_JSON))
              .andExpect(status().isInternalServerError());
    }

    @Test
    void testDeleteResource_WhenValidIds_ThenReturnOkWithDeletedIds() throws Exception {
        String validIds = "1,2,3";
        List<Long> deletedIdsList = Arrays.asList(1L, 2L, 3L);

        when(resourceRepository.findAllById(anyList())).thenReturn(deletedIdsList.stream().map(id -> {
            Resource resource = new Resource();
            resource.setId(id);
            return resource;
        }).collect(Collectors.toList()));

        mockMvc.perform(delete("/resources?id=" + validIds).accept(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk()).andExpect(jsonPath("$.ids", hasSize(3)))
              .andExpect(jsonPath("$.ids[0]", is(1))).andExpect(jsonPath("$.ids[1]", is(2)))
              .andExpect(jsonPath("$.ids[2]", is(3)));
    }

    @Test
    void testDeleteResource_WhenInvalidCsvLength_ThenReturnBadRequestStatus400() throws Exception {
        String longCsv = String.join(",", Collections.nCopies(200, "1"));

        mockMvc.perform(delete("/resources?id=" + longCsv).accept(MediaType.APPLICATION_JSON))
              .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteResource_WhenUnexpectedError_ThenReturnInternalServerStatus500() throws Exception {
        String validIds = "1,2,3";

        doThrow(new RuntimeException("Unexpected error")).when(resourceRepository).findAllById(anyList());

        mockMvc.perform(delete("/resources?id=" + validIds).accept(MediaType.APPLICATION_JSON))
              .andExpect(status().isInternalServerError());
    }
}