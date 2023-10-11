package com.tananushka.song.svc.service;

import com.tananushka.song.svc.dto.SongIdResponse;
import com.tananushka.song.svc.dto.SongRequest;
import com.tananushka.song.svc.dto.SongResponse;
import com.tananushka.song.svc.entity.Song;
import com.tananushka.song.svc.exception.SongServiceException;
import com.tananushka.song.svc.mapper.SongMapper;
import com.tananushka.song.svc.repository.SongRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SongServiceTest {

    @InjectMocks
    private SongService songService;

    @Spy
    private SongMapper songMapper = new SongMapper();

    @Mock
    private SongRepository songRepository;

    @Test
    void testSave_WhenGivenSongRequest_ThenReturnsSongIdResponse() {
        SongRequest songRequest = setupSongRequest();
        Song expectedSong = setupSong(null);
        Song savedSong = setupSong(1L);

        when(songRepository.save(ArgumentMatchers.argThat(
              song -> song.getName().equals(expectedSong.getName()) && song.getArtist()
                    .equals(expectedSong.getArtist()) && song.getAlbum()
                    .equals(expectedSong.getAlbum()) && song.getYear()
                    .equals(expectedSong.getYear()) && song.getDuration()
                    .equals(expectedSong.getDuration())))).thenReturn(savedSong);

        SongIdResponse response = songService.save(songRequest);

        assertEquals(1, response.getId());

        assertEquals(expectedSong.getName(), songRequest.getName());
        assertEquals(expectedSong.getArtist(), songRequest.getArtist());
        assertEquals(expectedSong.getAlbum(), songRequest.getAlbum());
        assertEquals(expectedSong.getYear(), songRequest.getYear());
        assertEquals(expectedSong.getDuration(), songRequest.getDuration());
    }

    @Test
    void testGetById_WhenValidIdAndSongExists_ThenReturnsSongResponse() {
        Integer id = 1;
        Song song = setupSong(1L);

        when(songRepository.existsById(id.longValue())).thenReturn(true);
        when(songRepository.findById(id.longValue())).thenReturn(Optional.of(song));

        SongResponse response = songService.getById(id);

        assertEquals(song.getId().intValue(), response.getId());
        assertEquals(song.getName(), response.getName());
        assertEquals(song.getArtist(), response.getArtist());
        assertEquals(song.getAlbum(), response.getAlbum());
        assertEquals(song.getYear(), response.getYear());
        assertEquals(song.getDuration(), response.getDuration());
    }

    @Test
    void testGetById_WhenInvalidId_ThenThrowsSongServiceException() {
        Integer id = 999;

        when(songRepository.existsById(id.longValue())).thenReturn(false);

        assertThrows(SongServiceException.class, () -> songService.getById(id));
    }

    @Test
    void testGetById_WhenRepositoryThrowsException_ThenPropagateTheException() {
        Integer id = 1;

        when(songRepository.existsById(id.longValue())).thenReturn(true);
        when(songRepository.findById(id.longValue())).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> songService.getById(id));
    }

    @Test
    void testDeleteSongs_WhenGivenValidCsvIdsAndAllSongsExist_ThenReturnsListOfIds() {
        String csvIds = "1,2,3";
        List<Long> ids = List.of(1L, 2L, 3L);

        when(songRepository.findAllById(ids)).thenReturn(setupListOfSongs());

        List<Long> returnedIds = songService.deleteSongs(csvIds);

        assertEquals(ids, returnedIds);
    }

    @Test
    void testDeleteSongs_WhenGivenCsvIdsAndSomeSongsDontExist_ThenReturnsListOfExistingIds() {
        String csvIds = "1,2,3,4";
        List<Long> ids = List.of(1L, 2L, 3L, 4L);
        List<Long> existingIds = List.of(1L, 2L, 3L);

        when(songRepository.findAllById(ids)).thenReturn(setupListOfSongs());

        List<Long> returnedIds = songService.deleteSongs(csvIds);

        assertEquals(existingIds, returnedIds);
    }

    @Test
    void testDeleteSongs_WhenGivenInvalidCsvIds_ThenThrowsSongServiceException() {
        String csvIds = "1,2,3," + "4".repeat(197);

        assertThrows(SongServiceException.class, () -> songService.deleteSongs(csvIds));
    }

    @Test
    void testDeleteSongs_WhenRepositoryThrowsException_ThenPropagateTheException() {
        String csvIds = "1,2,3";
        List<Long> ids = List.of(1L, 2L, 3L);

        when(songRepository.findAllById(ids)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> songService.deleteSongs(csvIds));
    }

    private SongRequest setupSongRequest() {
        SongRequest request = new SongRequest();
        request.setId(1);
        request.setArtist("Test Artist");
        request.setName("Test Song");
        request.setAlbum("Test Album");
        request.setYear("2020");
        request.setDuration("03:30");
        return request;
    }

    private Song setupSong(Long id) {
        Song song = new Song();
        song.setId(id);
        song.setResourceId(1);
        song.setArtist("Test Artist");
        song.setName("Test Song");
        song.setAlbum("Test Album");
        song.setYear("2020");
        song.setDuration("03:30");
        return song;
    }

    private SongResponse setupSongResponse() {
        SongResponse response = new SongResponse();
        response.setId(1);
        response.setName("Test Song");
        response.setArtist("Test Artist");
        response.setAlbum("Test Album");
        response.setDuration("03:30");
        response.setResourceId(10);
        response.setYear("2020");
        return response;
    }

    private SongIdResponse setupSongIdResponse() {
        return new SongIdResponse(1);
    }

    private List<Song> setupListOfSongs() {
        Song song1 = setupSong(1L);
        Song song2 = setupSong(2L);
        Song song3 = setupSong(3L);
        return List.of(song1, song2, song3);
    }
}
