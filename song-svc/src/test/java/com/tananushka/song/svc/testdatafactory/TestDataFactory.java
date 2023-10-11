package com.tananushka.song.svc.testdatafactory;

import com.tananushka.song.svc.dto.SongIdResponse;
import com.tananushka.song.svc.dto.SongRequest;
import com.tananushka.song.svc.dto.SongResponse;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestDataFactory {

    public static SongRequest createValidSongRequest() {
        SongRequest validSongRequest = new SongRequest();
        validSongRequest.setId(123);
        validSongRequest.setArtist("The Beatles");
        validSongRequest.setName("Let It Be");
        validSongRequest.setAlbum("Let It Be Album");
        validSongRequest.setYear("1970");
        validSongRequest.setDuration("04:03");
        return validSongRequest;
    }

    public static SongRequest createInvalidSongRequest() {
        SongRequest invalidSongRequest = new SongRequest();
        invalidSongRequest.setId(-1);
        invalidSongRequest.setArtist("The Beatles");
        invalidSongRequest.setName("Let It Be");
        invalidSongRequest.setAlbum("Let It Be Album");
        invalidSongRequest.setYear("197A");
        invalidSongRequest.setDuration("6602:03");
        return invalidSongRequest;
    }

    public static SongIdResponse createExpectedSongIdResponse() {
        return new SongIdResponse(1);
    }

    public static Integer createValidSongId() {
        return 123;
    }

    public static Integer createInvalidSongId() {
        return 456;
    }

    public static SongResponse createExpectedSongResponse(Integer id) {
        SongResponse expectedSongResponse = new SongResponse();
        expectedSongResponse.setId(id);
        expectedSongResponse.setArtist("The Beatles");
        expectedSongResponse.setName("Let It Be");
        expectedSongResponse.setAlbum("Let It Be Album");
        expectedSongResponse.setDuration("04:03");
        return expectedSongResponse;
    }

    public static String createValidCsvIds() {
        return "123,124,125";
    }

    public static String createValidCsvIds(Number... ids) {
        return Stream.of(ids).map(String::valueOf).collect(Collectors.joining(","));
    }

    public static String createInvalidCsvIds() {
        return "456";
    }

    public static List<Long> createExpectedDeletedIds() {
        return List.of(123L, 124L, 125L);
    }
}
