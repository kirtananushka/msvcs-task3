package com.tananushka.song.svc.repository;

import com.tananushka.song.svc.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    void deleteByIdIn(List<Long> ids);
}
