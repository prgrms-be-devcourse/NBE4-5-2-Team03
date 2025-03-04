package com.example.Flicktionary.domain.movie.repository;

import com.example.Flicktionary.domain.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByTmdbId(long tmdbId);
}
