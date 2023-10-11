package com.tananushka.song.svc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tananushka.song.svc.dto.SongRequest;
import com.tananushka.song.svc.entity.Song;
import com.tananushka.song.svc.mapper.SongMapper;
import com.tananushka.song.svc.repository.SongRepository;
import com.tananushka.song.svc.service.SongService;
import com.tananushka.song.svc.testdatafactory.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
class SongControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SongService songService;

    @Autowired
    private SongMapper songMapper;

    @MockBean
    private SongRepository songRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAddSong_WhenValidRequest_ThenReturnOk() throws Exception {
        SongRequest validSongRequest = TestDataFactory.createValidSongRequest();

        Song savedSong = new Song();
        savedSong.setId(123L);

        when(songRepository.save(any(Song.class))).thenReturn(savedSong);

        mockMvc.perform(MockMvcRequestBuilders.post("/songs").content(objectMapper.writeValueAsString(validSongRequest))
                    .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
              .andExpect(MockMvcResultMatchers.status().isOk())
              .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(123));
    }

    @Test
    void testAddSong_WhenInvalidRequest_ThenReturnBadRequest400() throws Exception {
        SongRequest invalidSongRequest = TestDataFactory.createInvalidSongRequest();

        mockMvc.perform(
                    MockMvcRequestBuilders.post("/songs").content(objectMapper.writeValueAsString(invalidSongRequest))
                          .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
              .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testAddSong_WhenUnexpectedError_ThenReturnInternalServerStatus500() throws Exception {
        SongRequest validSongRequest = TestDataFactory.createValidSongRequest();

        doThrow(new RuntimeException("Unexpected error")).when(songRepository).save(any());

        mockMvc.perform(MockMvcRequestBuilders.post("/songs").content(objectMapper.writeValueAsString(validSongRequest))
                    .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
              .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    void testGetSongById_WhenValidId_ThenReturnOk() throws Exception {
        SongRequest validSongRequest = TestDataFactory.createValidSongRequest();

        Song songEntity = songMapper.toEntity(validSongRequest);
        songEntity.setId(123L);

        when(songRepository.existsById(123L)).thenReturn(true);
        when(songRepository.findById(123L)).thenReturn(Optional.of(songEntity));

        mockMvc.perform(MockMvcRequestBuilders.get("/songs/" + songEntity.getId()).accept(MediaType.APPLICATION_JSON))
              .andExpect(MockMvcResultMatchers.status().isOk())
              .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(songEntity.getId()));

        verify(songRepository, times(1)).findById(songEntity.getId());
    }

    @Test
    void testGetSongById_WhenInvalidId_ThenReturnNotFound404() throws Exception {
        Integer invalidSongId = TestDataFactory.createInvalidSongId();

        mockMvc.perform(MockMvcRequestBuilders.get("/songs/" + invalidSongId).accept(MediaType.APPLICATION_JSON))
              .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testGetSongById_WhenUnexpectedError_ThenReturnInternalServerStatus500() throws Exception {
        Integer validSongId = TestDataFactory.createValidSongId();

        when(songRepository.existsById(anyLong())).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(songRepository).findById(anyLong());

        mockMvc.perform(MockMvcRequestBuilders.get("/songs/" + validSongId).accept(MediaType.APPLICATION_JSON))
              .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    void testDeleteSong_WhenValidIds_ThenReturnOkAndDeletedIds() throws Exception {
        SongRequest validSongRequest1 = TestDataFactory.createValidSongRequest();
        SongRequest validSongRequest2 = TestDataFactory.createValidSongRequest();
        validSongRequest2.setId(124);

        Song songEntity1 = songMapper.toEntity(validSongRequest1);
        Song songEntity2 = songMapper.toEntity(validSongRequest2);

        when(songRepository.save(songEntity1)).thenReturn(songEntity1);
        when(songRepository.save(songEntity2)).thenReturn(songEntity2);

        songEntity1.setId(123L);
        songEntity2.setId(124L);

        String validCsvIds = TestDataFactory.createValidCsvIds(songEntity1.getId(), songEntity2.getId());

        when(songRepository.findAllById(anyList())).thenReturn(List.of(songEntity1, songEntity2));
        doNothing().when(songRepository).deleteByIdIn(anyList());

        mockMvc.perform(
                    MockMvcRequestBuilders.delete("/songs").param("id", validCsvIds).accept(MediaType.APPLICATION_JSON))
              .andExpect(MockMvcResultMatchers.status().isOk())
              .andExpect(MockMvcResultMatchers.jsonPath("$.ids[0]").value(songEntity1.getId()))
              .andExpect(MockMvcResultMatchers.jsonPath("$.ids[1]").value(songEntity2.getId()));

        verify(songRepository, times(1)).deleteByIdIn(anyList());
    }

    @Test
    void testDeleteSong_WhenInvalidIds_ThenReturnOkAndEmptyList() throws Exception {
        String invalidCsvIds = TestDataFactory.createInvalidCsvIds();

        mockMvc.perform(
                    MockMvcRequestBuilders.delete("/songs").param("id", invalidCsvIds).accept(MediaType.APPLICATION_JSON))
              .andExpect(MockMvcResultMatchers.status().isOk())
              .andExpect(MockMvcResultMatchers.jsonPath("$.ids").isEmpty());
    }

    @Test
    void testDeleteSong_WhenInvalidIdFormat_ThenReturnBadRequest400() throws Exception {
        String invalidFormatCsvIds = "abc,def";

        mockMvc.perform(MockMvcRequestBuilders.delete("/songs").param("id", invalidFormatCsvIds)
              .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testDeleteSong_WhenUnexpectedError_ThenReturnInternalServerStatus500() throws Exception {
        SongRequest validSongRequest1 = TestDataFactory.createValidSongRequest();
        SongRequest validSongRequest2 = TestDataFactory.createValidSongRequest();
        validSongRequest2.setId(124);

        Song songEntity1 = songMapper.toEntity(validSongRequest1);
        Song songEntity2 = songMapper.toEntity(validSongRequest2);

        when(songRepository.save(songEntity1)).thenReturn(songEntity1);
        when(songRepository.save(songEntity2)).thenReturn(songEntity2);

        songEntity1.setId(123L);
        songEntity2.setId(124L);

        String validCsvIds = TestDataFactory.createValidCsvIds(songEntity1.getId(), songEntity2.getId());

        doThrow(new RuntimeException("Unexpected error")).when(songRepository).findAllById(anyList());

        mockMvc.perform(
                    MockMvcRequestBuilders.delete("/songs").param("id", validCsvIds).accept(MediaType.APPLICATION_JSON))
              .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }
}
