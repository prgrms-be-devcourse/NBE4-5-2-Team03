package com.example.Flicktionary.domain.series.repository;

import com.example.Flicktionary.domain.series.entity.SeriesCast;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeriesCastRepository extends JpaRepository<SeriesCast, Long> {

    List<SeriesCast> findSeriesByActorId(Long actorId);
}
