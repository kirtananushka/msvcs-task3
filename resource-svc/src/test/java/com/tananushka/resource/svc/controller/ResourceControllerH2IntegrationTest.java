package com.tananushka.resource.svc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResourceControllerH2IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ResourceRepository resourceRepository;

    @MockBean
    private SongClient songClient;

    @Test
    void testUploadResource_WhenValidAudio_ThenReturnOk() throws Exception {
        byte[] audioData = Files.readAllBytes(Path.of("src/test/resources/valid-audio-data.mp3"));
        resourceRepository.deleteAll();

        SongIdResponse songIdResponse = new SongIdResponse();
        songIdResponse.setId(1);
        when(songClient.postMetadata(any(SongRequest.class))).thenReturn(songIdResponse);

        MvcResult mvcResult = mockMvc.perform(post("/resources")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .content(audioData))
              .andExpect(status().isOk())
              .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        ResourceResponse returnedResponse = new ObjectMapper().readValue(responseBody, ResourceResponse.class);

        assertNotNull(returnedResponse.getId());

        Optional<Resource> savedResource = resourceRepository.findById(Long.valueOf(returnedResponse.getId()));
        assertTrue(savedResource.isPresent());
        assertArrayEquals(audioData, savedResource.get().getAudioData());
    }


    @Test
    void testUploadResource_WhenInvalidAudio_ThenReturnBadRequestStatus400() throws Exception {
        byte[] invalidAudioData = Files.readAllBytes(Path.of("src/test/resources/invalid-audio-data.mp3"));

        mockMvc.perform(post("/resources").contentType(MediaType.APPLICATION_OCTET_STREAM).content(invalidAudioData))
              .andExpect(status().isBadRequest());
    }

    @Test
    void testGetResource_WhenValidResourceId_ThenReturnAudioData() throws Exception {
        byte[] expectedAudioData = Files.readAllBytes(Path.of("src/test/resources/valid-audio-data.mp3"));
        resourceRepository.deleteAll();

        Resource retrievedResource = new Resource();
        retrievedResource.setAudioData(expectedAudioData);

        retrievedResource = resourceRepository.save(retrievedResource);

        mockMvc.perform(get("/resources/" + retrievedResource.getId()).accept(MediaType.APPLICATION_OCTET_STREAM))
              .andExpect(status().isOk()).andExpect(result -> {
                  byte[] returnedAudioData = result.getResponse().getContentAsByteArray();
                  assertThat(returnedAudioData, equalTo(expectedAudioData));
              });
    }

    @Test
    void testGetResource_WhenInvalidResourceId_ThenReturnNotFoundStatus404() throws Exception {
        long invalidResourceId = 999L;
        resourceRepository.deleteAll();

        mockMvc.perform(get("/resources/" + invalidResourceId).accept(MediaType.APPLICATION_JSON))
              .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteResource_WhenValidIds_ThenReturnOkWithDeletedIds() throws Exception {
        List<Resource> resourcesToSave = Arrays.asList(new Resource(1L, "data1".getBytes()),
              new Resource(2L, "data2".getBytes()), new Resource(3L, "data3".getBytes()));
        resourceRepository.deleteAll();

        List<Resource> savedResources = resourceRepository.saveAll(resourcesToSave);

        List<Long> savedResourceIds = savedResources.stream().map(Resource::getId).toList();

        String idsToDelete = String.join(",", savedResourceIds.stream().map(String::valueOf).toList());

        mockMvc.perform(delete("/resources?id=" + idsToDelete).accept(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk()).andExpect(jsonPath("$.ids", hasSize(3)))
              .andExpect(jsonPath("$.ids[0]", is(savedResourceIds.get(0).intValue())))
              .andExpect(jsonPath("$.ids[1]", is(savedResourceIds.get(1).intValue())))
              .andExpect(jsonPath("$.ids[2]", is(savedResourceIds.get(2).intValue())));
    }

    @Test
    void testDeleteResource_WhenInvalidCsvLength_ThenReturnBadRequestStatus400() throws Exception {
        String longCsv = String.join(",", Collections.nCopies(200, "1"));

        mockMvc.perform(delete("/resources?id=" + longCsv).accept(MediaType.APPLICATION_JSON))
              .andExpect(status().isBadRequest());
    }
}