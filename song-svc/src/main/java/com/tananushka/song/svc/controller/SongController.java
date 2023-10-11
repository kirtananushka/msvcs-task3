package com.tananushka.song.svc.controller;

import com.tananushka.song.svc.dto.SongIdResponse;
import com.tananushka.song.svc.dto.SongRequest;
import com.tananushka.song.svc.dto.SongResponse;
import com.tananushka.song.svc.service.SongService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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
@RequestMapping("/songs")
@AllArgsConstructor
public class SongController {

    private final SongService songService;

    @PostMapping
    public ResponseEntity<SongIdResponse> addSong(@Valid @RequestBody SongRequest songRequest) {
        SongIdResponse response = songService.save(songRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongResponse> getSongById(@PathVariable Integer id) {
        SongResponse songResponse = songService.getById(id);
        return ResponseEntity.ok(songResponse);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Long>>> deleteSong(@RequestParam String id) {
        List<Long> deletedIds = songService.deleteSongs(id);
        Map<String, List<Long>> response = Collections.singletonMap("ids", deletedIds);
        return ResponseEntity.ok(response);
    }
}
