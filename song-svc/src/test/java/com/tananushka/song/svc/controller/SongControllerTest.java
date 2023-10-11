package com.tananushka.song.svc.controller;

import com.tananushka.song.svc.dto.SongIdResponse;
import com.tananushka.song.svc.dto.SongRequest;
import com.tananushka.song.svc.dto.SongResponse;
import com.tananushka.song.svc.exception.SongServiceException;
import com.tananushka.song.svc.service.SongService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.tananushka.song.svc.testdatafactory.TestDataFactory.createExpectedDeletedIds;
import static com.tananushka.song.svc.testdatafactory.TestDataFactory.createExpectedSongIdResponse;
import static com.tananushka.song.svc.testdatafactory.TestDataFactory.createExpectedSongResponse;
import static com.tananushka.song.svc.testdatafactory.TestDataFactory.createInvalidCsvIds;
import static com.tananushka.song.svc.testdatafactory.TestDataFactory.createInvalidSongId;
import static com.tananushka.song.svc.testdatafactory.TestDataFactory.createInvalidSongRequest;
import static com.tananushka.song.svc.testdatafactory.TestDataFactory.createValidCsvIds;
import static com.tananushka.song.svc.testdatafactory.TestDataFactory.createValidSongId;
import static com.tananushka.song.svc.testdatafactory.TestDataFactory.createValidSongRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SongControllerTest {

    @InjectMocks
    private SongController songController;

    @Mock
    private SongService songService;

    @Test
    void testAddSong_WhenValidSongRequest_ThenReturnOk() {
        SongRequest validSongRequest = createValidSongRequest();
        SongIdResponse expectedResponse = createExpectedSongIdResponse();

        when(songService.save(validSongRequest)).thenReturn(expectedResponse);

        ResponseEntity<SongIdResponse> result = songController.addSong(validSongRequest);
        assertEquals(ResponseEntity.ok(expectedResponse), result);
    }

    @Test
    void testAddSong_WhenInvalidSongRequest_ThenReturnBadRequest() {
        SongRequest invalidSongRequest = createInvalidSongRequest();

        when(songService.save(invalidSongRequest)).thenThrow(new SongServiceException("Invalid song request", "400"));

        Exception exception = assertThrows(SongServiceException.class,
              () -> songController.addSong(invalidSongRequest));
        assertTrue(exception.getMessage().contains("Invalid song request"));
    }

    @Test
    void testAddSong_WhenUnexpectedError_ThenReturnError() {
        SongRequest validSongRequest = createValidSongRequest();

        when(songService.save(validSongRequest)).thenThrow(new RuntimeException("Unexpected error"));

        Exception exception = assertThrows(RuntimeException.class, () -> songController.addSong(validSongRequest));
        assertEquals("Unexpected error", exception.getMessage());
    }

    @Test
    void testGetSongById_WhenValidId_ThenReturnSongResponse() {
        Integer validSongId = createValidSongId();
        SongResponse expectedSongResponse = createExpectedSongResponse(validSongId);

        when(songService.getById(validSongId)).thenReturn(expectedSongResponse);

        ResponseEntity<SongResponse> result = songController.getSongById(validSongId);
        assertEquals(ResponseEntity.ok(expectedSongResponse), result);
    }

    @Test
    void testGetSongById_WhenInvalidId_ThenReturnNotFound() {
        Integer invalidSongId = createInvalidSongId();

        when(songService.getById(invalidSongId)).thenThrow(new SongServiceException("Song not found", "404"));

        Exception exception = assertThrows(SongServiceException.class, () -> songController.getSongById(invalidSongId));
        assertTrue(exception.getMessage().contains("Song not found"));
    }

    @Test
    void testGetSongById_WhenUnexpectedError_ThenReturnError() {
        Integer validSongId = createValidSongId();

        when(songService.getById(validSongId)).thenThrow(new RuntimeException("Unexpected error"));

        Exception exception = assertThrows(RuntimeException.class, () -> songController.getSongById(validSongId));
        assertEquals("Unexpected error", exception.getMessage());
    }

    @Test
    void testDeleteSong_WhenValidIds_ThenReturnDeletedIds() {
        String validCsvIds = createValidCsvIds();
        List<Long> expectedDeletedIds = createExpectedDeletedIds();

        when(songService.deleteSongs(validCsvIds)).thenReturn(expectedDeletedIds);

        ResponseEntity<Map<String, List<Long>>> result = songController.deleteSong(validCsvIds);
        assertEquals(ResponseEntity.ok(Collections.singletonMap("ids", expectedDeletedIds)), result);
    }

    @Test
    void testDeleteSong_WhenInvalidIds_ThenReturnNotFound() {
        String invalidCsvIds = createInvalidCsvIds();

        when(songService.deleteSongs(invalidCsvIds)).thenThrow(new SongServiceException("Songs not found", "404"));

        Exception exception = assertThrows(SongServiceException.class, () -> songController.deleteSong(invalidCsvIds));
        assertTrue(exception.getMessage().contains("Songs not found"));
    }

    @Test
    void testDeleteSong_WhenUnexpectedError_ThenReturnError() {
        String validCsvIds = createValidCsvIds();

        when(songService.deleteSongs(validCsvIds)).thenThrow(new RuntimeException("Unexpected error"));

        Exception exception = assertThrows(RuntimeException.class, () -> songController.deleteSong(validCsvIds));
        assertEquals("Unexpected error", exception.getMessage());
    }
}