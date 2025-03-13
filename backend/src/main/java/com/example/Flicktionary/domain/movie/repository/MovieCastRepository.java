package com.example.Flicktionary.domain.movie.repository;

import com.example.Flicktionary.domain.movie.entity.MovieCast;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieCastRepository extends JpaRepository<MovieCast, Long> {

    List<MovieCast> findMoviesByActorId(Long actorId);
}
