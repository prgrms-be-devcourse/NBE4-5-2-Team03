package com.example.Flicktionary.domain.review.repository;

import com.example.Flicktionary.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 영화에 대한 리뷰를 페이징 처리해서 가져옴
    Page<Review> findByMovieId(Long movie_id, Pageable pageable);

    // 특정 드라마에 대한 리뷰를 페이징 처리해서 가져옴
    Page<Review> findBySeriesId(Long series_id, Pageable pageable);
}
