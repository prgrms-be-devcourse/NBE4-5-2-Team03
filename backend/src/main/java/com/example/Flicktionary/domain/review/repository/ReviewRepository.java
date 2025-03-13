package com.example.Flicktionary.domain.review.repository;

import com.example.Flicktionary.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 영화에 대한 리뷰를 페이징 처리해서 가져옴
    @EntityGraph(attributePaths = {"userAccount"}) // 최적화를 위해 EntityGraph 사용
    Page<Review> findByMovie_IdOrderByIdDesc(Long movieId, Pageable pageable);

    // 특정 드라마에 대한 리뷰를 페이징 처리해서 가져옴
    @EntityGraph(attributePaths = {"userAccount"}) // 최적화를 위해 EntityGraph 사용
    Page<Review> findBySeries_IdOrderByIdDesc(Long seriesId, Pageable pageable);

    // 닉네임 또는 리뷰 내용으로 검색하는 기능
    Page<Review> findByUserAccount_NicknameContainingOrContentContaining(String nickname, String content, Pageable pageable);

    // 특정 영화의 평균 평점을 계산하는 기능
//    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.movie.id = :movieId")
//    Double findAverageRatingByMovie_Id(Long movieId);

    // 특정 드라마의 평균 평점을 계산하는 기능
//    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.series.id = :seriesId")
//    Double findAverageRatingBySeries_Id(Long seriesId);

    // 특정 영화의 리뷰 개수를 세는 기능
//    long countByMovie_Id(Long movieId);

    // 특정 드라마의 리뷰 개수를 세는 기능
//    long countBySeries_Id(Long seriesId);
}