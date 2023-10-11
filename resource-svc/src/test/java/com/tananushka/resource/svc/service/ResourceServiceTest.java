package com.tananushka.resource.svc.service;

import com.tananushka.resource.svc.client.SongClient;
import com.tananushka.resource.svc.dto.ResourceResponse;
import com.tananushka.resource.svc.dto.SongIdResponse;
import com.tananushka.resource.svc.dto.SongRequest;
import com.tananushka.resource.svc.entity.Resource;
import com.tananushka.resource.svc.exception.ResourceServiceException;
import com.tananushka.resource.svc.mapper.ResourceMapper;
import com.tananushka.resource.svc.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @InjectMocks
    private ResourceService resourceService;

    @Mock
    private ResourceMapper resourceMapper;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private SongClient songClient;

    @Test
    void testSaveResource_WhenValidAudio_ThenReturnsResponse() throws IOException {
        byte[] audioData = Files.readAllBytes(Path.of("src/test/resources/valid-audio-data.mp3"));
        Resource savedResource = new Resource();
        savedResource.setId(1L);

        ResourceResponse expectedResponse = new ResourceResponse();
        expectedResponse.setId(1);

        SongRequest songRequest = new SongRequest();
        SongIdResponse songIdResponse = new SongIdResponse();
        songIdResponse.setId(1);

        when(resourceRepository.save(any(Resource.class))).thenReturn(savedResource);
        when(resourceMapper.toRequest(anyLong(), any())).thenReturn(songRequest);
        when(songClient.postMetadata(any(SongRequest.class))).thenReturn(songIdResponse);
        when(resourceMapper.toResponse(any(Resource.class))).thenReturn(expectedResponse);

        ResourceResponse actualResponse = resourceService.saveResource(audioData);

        assertEquals(expectedResponse, actualResponse);

        verify(resourceRepository).save(any(Resource.class));
        verify(songClient).postMetadata(any(SongRequest.class));
    }

    @Test
    void testGetResourceData_WhenIdExists_ThenReturnsAudioData() throws IOException {
        Integer id = 1;
        byte[] expectedAudioData = Files.readAllBytes(Path.of("src/test/resources/valid-audio-data.mp3"));
        Resource resource = new Resource();
        resource.setId(1L);
        resource.setAudioData(expectedAudioData);

        when(resourceRepository.existsById(anyLong())).thenReturn(true);
        when(resourceRepository.findById(anyLong())).thenReturn(Optional.of(resource));

        byte[] actualAudioData = resourceService.getResourceData(id);

        assertEquals(expectedAudioData, actualAudioData);
    }

    @Test
    void testGetResourceData_WhenIdDoesNotExist_ThenThrowsException() {
        Integer id = 1;
        when(resourceRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(ResourceServiceException.class, () -> resourceService.getResourceData(id));
    }

    @Test
    void testDeleteResources_WhenValidCsv_ThenDeletesAndReturnsIds() {
        String csvIds = "1,2,3";
        List<Long> expectedIds = List.of(1L, 2L, 3L);
        Resource resource1 = new Resource();
        resource1.setId(1L);
        Resource resource2 = new Resource();
        resource2.setId(2L);
        Resource resource3 = new Resource();
        resource3.setId(3L);

        when(resourceRepository.findAllById(anyList())).thenReturn(Arrays.asList(resource1, resource2, resource3));

        List<Long> actualIds = resourceService.deleteResources(csvIds);

        assertEquals(expectedIds, actualIds);
        verify(resourceRepository).deleteByIdIn(expectedIds);
    }

    @Test
    void testDeleteResources_WhenSomeIdsNonExistent_ThenOnlyDeletesExistentWithoutException() {
        String csvIds = "1,2,5";
        List<Long> expectedIds = List.of(1L, 2L);
        Resource resource1 = new Resource();
        resource1.setId(1L);
        Resource resource2 = new Resource();
        resource2.setId(2L);

        when(resourceRepository.findAllById(anyList())).thenReturn(Arrays.asList(resource1, resource2));

        List<Long> actualIds = resourceService.deleteResources(csvIds);

        assertEquals(expectedIds, actualIds);
        verify(resourceRepository).deleteByIdIn(expectedIds);
    }

    @Test
    void testDeleteResources_WhenInvalidCsv_ThenThrowsException() {
        String csvIds = String.join(",", Collections.nCopies(250, "1"));

        assertThrows(ResourceServiceException.class, () -> resourceService.deleteResources(csvIds));
    }
}

