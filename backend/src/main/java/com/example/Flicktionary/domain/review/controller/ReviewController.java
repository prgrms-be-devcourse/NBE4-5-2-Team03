package com.example.Flicktionary.domain.review.controller;

import com.example.Flicktionary.domain.review.dto.ReviewDto;
import com.example.Flicktionary.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 생성
    @PostMapping
    public ReviewDto createReview(@RequestBody ReviewDto reviewDto) {
        return reviewService.createReview(reviewDto);
    }

    // 모든 리뷰 조회
    @GetMapping
    public List<ReviewDto> getAllReviews() {
        return reviewService.findAllReviews();
    }

    // 리뷰 수정
    @PutMapping("/{id}")
    public ReviewDto updateReview(@PathVariable Long id, @RequestBody ReviewDto reviewDto) {
        return reviewService.updateReview(id, reviewDto);
    }

    // 리뷰 삭제
    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }

    // 특정 영화의 리뷰를 페이지로 조회 (0 ~ 5 페이지)
    @GetMapping("/movie/{movie_id}")
    public ResponseEntity<Page<ReviewDto>> reviewMovieDtoPage(
            @PathVariable Long movie_id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(reviewService.reviewMovieDtoPage(movie_id, page, size));
    }

    // 특정 드라마의 리뷰를 페이지로 조회 (0 ~ 5 페이지)
    @GetMapping("/series/{series_id}")
    public ResponseEntity<Page<ReviewDto>> reviewSeriesDtoPage(
            @PathVariable Long series_id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(reviewService.reviewSeriesDtoPage(series_id, page, size));
    }
}
