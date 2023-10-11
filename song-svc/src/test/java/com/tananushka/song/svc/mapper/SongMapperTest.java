package com.tananushka.song.svc.mapper;

import com.tananushka.song.svc.dto.SongIdResponse;
import com.tananushka.song.svc.dto.SongRequest;
import com.tananushka.song.svc.dto.SongResponse;
import com.tananushka.song.svc.entity.Song;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SongMapperTest {

    private final SongMapper songMapper = new SongMapper();

    @Test
    void testToEntity_WhenGivenSongRequest_ThenReturnsSong() {
        SongRequest songRequest = setupSongRequest();

        Song song = songMapper.toEntity(songRequest);

        assertEquals(songRequest.getId(), song.getResourceId());
        assertEquals(songRequest.getArtist(), song.getArtist());
        assertEquals(songRequest.getName(), song.getName());
        assertEquals(songRequest.getAlbum(), song.getAlbum());
        assertEquals(songRequest.getYear(), song.getYear());
        assertEquals(songRequest.getDuration(), song.getDuration());
    }

    @Test
    void testToResponse_WhenGivenSong_ThenReturnsSongResponse() {
        Song song = setupSong();

        SongResponse response = songMapper.toResponse(song);

        assertEquals(song.getId().intValue(), response.getId());
        assertEquals(song.getName(), response.getName());
        assertEquals(song.getArtist(), response.getArtist());
        assertEquals(song.getAlbum(), response.getAlbum());
        assertEquals(song.getDuration(), response.getDuration());
        assertEquals(song.getResourceId(), response.getResourceId());
        assertEquals(song.getYear(), response.getYear());
    }

    @Test
    void testToIdResponse_WhenGivenSong_ThenReturnsSongIdResponse() {
        Song song = setupSong();

        SongIdResponse response = songMapper.toIdResponse(song);

        assertEquals(song.getId().intValue(), response.getId());
    }

    private SongRequest setupSongRequest() {
        SongRequest songRequest = new SongRequest();
        songRequest.setId(1);
        songRequest.setArtist("ArtistName");
        songRequest.setName("SongName");
        songRequest.setAlbum("AlbumName");
        songRequest.setYear("2022");
        songRequest.setDuration("03:15");
        return songRequest;
    }

    private Song setupSong() {
        Song song = new Song();
        song.setId(1L);
        song.setResourceId(1);
        song.setArtist("ArtistName");
        song.setName("SongName");
        song.setAlbum("AlbumName");
        song.setYear("2022");
        song.setDuration("03:15");
        return song;
    }
}
