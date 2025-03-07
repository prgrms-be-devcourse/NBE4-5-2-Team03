package com.example.Flicktionary.domain.series.repository;

import com.example.Flicktionary.domain.series.entity.Series;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeriesRepository extends JpaRepository<Series, Long> {
    Optional<Series> findById(Long id);

    @Query("SELECT s FROM Series s WHERE s.title LIKE %:keyword%")
    Page<Series> findByTitleLike(@Param("keyword") String keyword, Pageable pageable);
}
