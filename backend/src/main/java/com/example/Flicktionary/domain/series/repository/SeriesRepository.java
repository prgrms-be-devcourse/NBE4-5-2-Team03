package com.example.Flicktionary.domain.series.repository;

import com.example.Flicktionary.domain.series.domain.Series;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesRepository extends JpaRepository<Series, Long> {
}
