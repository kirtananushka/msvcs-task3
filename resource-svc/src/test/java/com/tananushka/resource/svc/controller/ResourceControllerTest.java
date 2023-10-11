package com.tananushka.resource.svc.controller;

import com.tananushka.resource.svc.dto.ResourceResponse;
import com.tananushka.resource.svc.exception.ResourceServiceException;
import com.tananushka.resource.svc.service.ResourceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceControllerTest {

    @InjectMocks
    private ResourceController resourceController;

    @Mock
    private ResourceService resourceServiceMock;

    @Test
    void testUploadResource_WhenValidAudio_ThenReturnOk() {
        byte[] validAudioData = new byte[10];
        ResourceResponse mockResponse = new ResourceResponse();
        mockResponse.setId(1);
        when(resourceServiceMock.saveResource(validAudioData)).thenReturn(mockResponse);

        ResponseEntity<ResourceResponse> response = resourceController.uploadResource(validAudioData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, Objects.requireNonNull(response.getBody()).getId());
    }

    @Test
    void testUploadResourceWhenInvalidAudioThenThrowResourceServiceException() {
        byte[] invalidAudioData = new byte[10];
        when(resourceServiceMock.saveResource(invalidAudioData)).thenThrow(
              new ResourceServiceException("Invalid audio data: some-mime-type", "400"));

        assertThrows(ResourceServiceException.class, () -> resourceController.uploadResource(invalidAudioData));
    }

    @Test
    void testUploadResourceWhenServiceThrowsUnexpectedExceptionThenThrowRuntimeException() {
        byte[] someAudioData = new byte[10];
        when(resourceServiceMock.saveResource(someAudioData)).thenThrow(new RuntimeException("Unexpected error"));

        assertThrows(RuntimeException.class, () -> resourceController.uploadResource(someAudioData));
    }

    @Test
    void testGetResourceWhenIdExistsThenReturnAudioData() {
        Integer resourceId = 1;
        byte[] mockAudioData = new byte[10];
        when(resourceServiceMock.getResourceData(resourceId)).thenReturn(mockAudioData);

        ResponseEntity<byte[]> response = resourceController.getResource(resourceId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(mockAudioData, response.getBody());
        assertEquals(MediaType.valueOf("audio/mpeg"), response.getHeaders().getContentType());
    }

    @Test
    void testGetResourceWhenIdDoesNotExistThenThrowResourceServiceException() {
        Integer nonExistentResourceId = 999;
        when(resourceServiceMock.getResourceData(nonExistentResourceId))
              .thenThrow(new ResourceServiceException(String.format("Resource with ID=%d not found", nonExistentResourceId), "404"));

        assertThrows(ResourceServiceException.class, () -> resourceController.getResource(nonExistentResourceId));
    }

    @Test
    void testGetResourceWhenUnexpectedExceptionOccursThenThrowRuntimeException() {
        Integer someResourceId = 2;
        when(resourceServiceMock.getResourceData(someResourceId))
              .thenThrow(new RuntimeException("Unexpected error"));

        assertThrows(RuntimeException.class, () -> resourceController.getResource(someResourceId));
    }

    @Test
    void testDeleteResource_WhenValidIds_ThenReturnOkWithDeletedIds() {
        String validIds = "1,2,3";
        List<Long> mockDeletedIds = Arrays.asList(1L, 2L, 3L);

        when(resourceServiceMock.deleteResources(validIds)).thenReturn(mockDeletedIds);

        ResponseEntity<Map<String, List<Long>>> response = resourceController.deleteResource(validIds);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockDeletedIds, response.getBody().get("ids"));
    }

    @Test
    void testDeleteResource_WhenInvalidCsvLength_ThenThrowResourceServiceException() {
        String longCsv = String.join(",", Collections.nCopies(200, "1"));

        when(resourceServiceMock.deleteResources(longCsv)).thenThrow(
              new ResourceServiceException("CSV length is too long", "400"));

        assertThrows(ResourceServiceException.class, () -> resourceController.deleteResource(longCsv));
    }

    @Test
    void testDeleteResource_WhenUnexpectedExceptionOccurs_ThenThrowRuntimeException() {
        String validIds = "1,2,3";

        when(resourceServiceMock.deleteResources(validIds)).thenThrow(new RuntimeException("Unexpected error"));

        assertThrows(RuntimeException.class, () -> resourceController.deleteResource(validIds));
    }
}
