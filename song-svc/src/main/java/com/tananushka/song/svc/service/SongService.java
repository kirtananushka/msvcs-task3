package com.tananushka.song.svc.service;

import com.tananushka.song.svc.dto.SongIdResponse;
import com.tananushka.song.svc.dto.SongRequest;
import com.tananushka.song.svc.dto.SongResponse;
import com.tananushka.song.svc.entity.Song;
import com.tananushka.song.svc.exception.SongServiceException;
import com.tananushka.song.svc.mapper.SongMapper;
import com.tananushka.song.svc.repository.SongRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@AllArgsConstructor
@Service
@Slf4j
public class SongService {

    private final SongMapper songMapper;

    private final SongRepository songRepository;

    @Transactional
    public SongIdResponse save(SongRequest songRequest) {
        Song song = songMapper.toEntity(songRequest);
        Song savedSong = songRepository.save(song);
        return songMapper.toIdResponse(savedSong);
    }

    public SongResponse getById(Integer id) {
        validateSongExistence(id);
        return songRepository.findById(id.longValue()).map(songMapper::toResponse)
              .orElseThrow(() -> new SongServiceException("Unexpected error", "500"));
    }

    @Transactional
    public List<Long> deleteSongs(String csvIds) {
        validateCsvIdsString(csvIds);
        List<Long> ids = parseCsvIds(csvIds);
        List<Long> existingIds = validateResourceExistence(ids);
        songRepository.deleteByIdIn(existingIds);
        return existingIds;
    }

    private void validateSongExistence(Integer id) {
        if (!songRepository.existsById(Long.valueOf(id))) {
            throw new SongServiceException(String.format("Song with ID=%d not found", id), "404");
        }
    }

    private void validateCsvIdsString(String csvIds) {
        if (csvIds.length() >= 200) {
            throw new SongServiceException("Invalid CSV length", "400");
        }
    }

    private List<Long> parseCsvIds(String csvIds) {
        return Stream.of(csvIds.split(",")).map(this::safeParseLong).toList();
    }

    private Long safeParseLong(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new SongServiceException(String.format("Invalid ID format: '%s'", id), "400");
        }
    }

    private List<Long> validateResourceExistence(List<Long> ids) {
        return songRepository.findAllById(ids).stream().map(Song::getId).toList();
    }
}

