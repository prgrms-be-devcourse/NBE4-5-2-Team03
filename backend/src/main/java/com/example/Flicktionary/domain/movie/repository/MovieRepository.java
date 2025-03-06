package com.example.Flicktionary.domain.movie.repository;

import com.example.Flicktionary.domain.movie.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByTmdbId(long tmdbId);

    @Query("SELECT m FROM Movie m WHERE m.title LIKE CONCAT('%', :keyword, '%')")
    Page<Movie> findByTitleLike(String keyword, Pageable pageable);

    // fetch join을 이용해서 효율적으로 데이터 가져오기
    @Query("SELECT m FROM Movie m " +
            "LEFT JOIN FETCH m.actors a " +
            "LEFT JOIN FETCH m.director d " +
            "WHERE m.id = :id")
    Optional<Movie> findByIdWithActorsAndGenresAndDirector(@Param("id") Long id);
}
