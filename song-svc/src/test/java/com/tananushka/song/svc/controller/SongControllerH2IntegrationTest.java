package com.tananushka.song.svc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tananushka.song.svc.dto.SongIdResponse;
import com.tananushka.song.svc.dto.SongRequest;
import com.tananushka.song.svc.repository.SongRepository;
import com.tananushka.song.svc.service.SongService;
import com.tananushka.song.svc.testdatafactory.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
class SongControllerH2IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SongService songService;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAddSong_WhenValidRequest_ThenReturnOk() throws Exception {
        SongRequest validSongRequest = TestDataFactory.createValidSongRequest();
        songRepository.deleteAll();

        mockMvc.perform(MockMvcRequestBuilders.post("/songs").content(objectMapper.writeValueAsString(validSongRequest))
                    .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
              .andExpect(MockMvcResultMatchers.status().isOk())
              .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }

    @Test
    void testAddSong_WhenInvalidRequest_ThenReturnBadRequest() throws Exception {
        SongRequest invalidSongRequest = TestDataFactory.createInvalidSongRequest();
        songRepository.deleteAll();

        mockMvc.perform(
                    MockMvcRequestBuilders.post("/songs").content(objectMapper.writeValueAsString(invalidSongRequest))
                          .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
              .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testGetSongById_WhenValidId_ThenReturnOk() throws Exception {
        SongRequest validSongRequest = TestDataFactory.createValidSongRequest();
        songRepository.deleteAll();

        SongIdResponse songIdResponse = songService.save(validSongRequest);

        mockMvc.perform(
                    MockMvcRequestBuilders.get("/songs/" + songIdResponse.getId()).accept(MediaType.APPLICATION_JSON))
              .andExpect(MockMvcResultMatchers.status().isOk())
              .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(songIdResponse.getId()));
    }

    @Test
    void testGetSongById_WhenInvalidId_ThenReturnNotFound() throws Exception {
        Integer invalidSongId = TestDataFactory.createInvalidSongId();
        songRepository.deleteAll();

        mockMvc.perform(MockMvcRequestBuilders.get("/songs/" + invalidSongId).accept(MediaType.APPLICATION_JSON))
              .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testDeleteSong_WhenValidIds_ThenReturnOkAndDeletedIds() throws Exception {
        SongRequest validSongRequest1 = TestDataFactory.createValidSongRequest();
        SongRequest validSongRequest2 = TestDataFactory.createValidSongRequest();
        validSongRequest2.setId(124);
        songRepository.deleteAll();

        SongIdResponse songId1 = songService.save(validSongRequest1);
        SongIdResponse songId2 = songService.save(validSongRequest2);

        String validCsvIds = TestDataFactory.createValidCsvIds(songId1.getId(), songId2.getId());

        mockMvc.perform(
                    MockMvcRequestBuilders.delete("/songs").param("id", validCsvIds).accept(MediaType.APPLICATION_JSON))
              .andExpect(MockMvcResultMatchers.status().isOk())
              .andExpect(MockMvcResultMatchers.jsonPath("$.ids[0]").value(songId1.getId()))
              .andExpect(MockMvcResultMatchers.jsonPath("$.ids[1]").value(songId2.getId()));
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
    void testDeleteSong_WhenInvalidIdFormat_ThenReturnBadRequest() throws Exception {
        String invalidFormatCsvIds = "abc,def";

        mockMvc.perform(MockMvcRequestBuilders.delete("/songs").param("id", invalidFormatCsvIds)
              .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
