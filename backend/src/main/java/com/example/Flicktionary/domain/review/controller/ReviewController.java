package com.example.Flicktionary.domain.review.controller;

import com.example.Flicktionary.domain.review.dto.ReviewDto;
import com.example.Flicktionary.domain.review.service.ReviewService;
import com.example.Flicktionary.global.dto.PageDto;
import com.example.Flicktionary.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 생성
    @PostMapping
    public ResponseEntity<ResponseDto<ReviewDto>> createReview(@RequestBody ReviewDto reviewDto) {
        ReviewDto review = reviewService.createReview(reviewDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.of(HttpStatus.CREATED.value() + "", HttpStatus.CREATED.getReasonPhrase(), review));
    }

    // 모든 리뷰 조회
    @GetMapping
    public ResponseEntity<ResponseDto<List<ReviewDto>>> getAllReviews() {
        List<ReviewDto> reviews = reviewService.findAllReviews();
        return ResponseEntity.ok(ResponseDto.ok(reviews));
    }

    // 리뷰 닉네임과 내용으로 검색
    @GetMapping("/search")
    public ResponseEntity<ResponseDto<PageDto<ReviewDto>>> searchReviews(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(ResponseDto.ok(reviewService.searchReviews(keyword, page, size)));
    }

    // 리뷰 수정
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<ReviewDto>> updateReview(
            @PathVariable Long id,
            @RequestBody ReviewDto reviewDto) {
        ReviewDto review = reviewService.updateReview(id, reviewDto);
        return ResponseEntity.ok(ResponseDto.ok(review));
    }

    // 리뷰 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<?>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ResponseDto.of(HttpStatus.NO_CONTENT.value() + "", HttpStatus.NO_CONTENT.getReasonPhrase()));
    }

    // TODO: 예외 처리를 서비스 레이어로 옮기는 것을 검토
    // 특정 영화의 리뷰를 페이지로 조회 (0 ~ 5 페이지)
    @GetMapping("/movie/{movie_id}")
    public ResponseEntity<ResponseDto<PageDto<ReviewDto>>> reviewMovieDtoPage(
            @PathVariable Long movie_id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        try {

            // 특정 영화 리뷰 페이지를 조회해 변수에 담아 클라이언트에 반환
            PageDto<ReviewDto> reviews = reviewService.reviewMovieDtoPage(movie_id, page, size);
            return ResponseEntity.ok(ResponseDto.ok(reviews));
        } catch (NoSuchElementException e) {
            // TODO: Spring에서 제공하는 예외를 바로 던질지, 내부에서 따로 예외를 처리할지 여부를 검토
            // 영화를 찾을 수 없을 경우 404 상태 반환
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "영화를 찾을 수 없습니다.");
        } catch (IllegalArgumentException e) {

            // 잘못된 파라미터가 전달된 경우 400 상태 반환
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 파라미터입니다.");
        } catch (Exception e) {

            // 그 이외의 예외 발생 시 500 상태 반환 및 로그 기록
            System.out.println("영화 리뷰 조회 중 오류가 발생했습니다: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "영화 리뷰 조회 중 오류가 발생했습니다.");
        }
    }

    // 특정 드라마의 리뷰를 페이지로 조회 (0 ~ 5 페이지)
    @GetMapping("/series/{series_id}")
    public ResponseEntity<ResponseDto<PageDto<ReviewDto>>> reviewSeriesDtoPage(
            @PathVariable Long series_id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        try {

            // 특정 드라마 리뷰 페이지를 조회해 변수에 담아 클라이언트에 반환
            PageDto<ReviewDto> reviews = reviewService.reviewSeriesDtoPage(series_id, page, size);
            return ResponseEntity.ok(ResponseDto.ok(reviews));
        } catch (NoSuchElementException e) {

            // 드라마를 찾을 수 없을 경우 404 상태 반환
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "드라마를 찾을 수 없습니다.");
        } catch (IllegalArgumentException e) {

            // 잘못된 파라미터가 전달된 경우 400 상태 반환
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 파라미터입니다.");
        } catch (Exception e) {

            // 그 이외의 예외 발생 시 500 상태 반환 및 로그 기록
            System.out.println("드라마 리뷰 조회 중 오류가 발생했습니다: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "드라마 리뷰 조회 중 오류가 발생했습니다.");
        }
    }
}