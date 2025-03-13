package com.example.Flicktionary.domain.review.service;

import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.review.dto.ReviewDto;
import com.example.Flicktionary.domain.review.entity.Review;
import com.example.Flicktionary.domain.review.repository.ReviewRepository;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.repository.SeriesRepository;
import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.repository.UserAccountRepository;
import com.example.Flicktionary.global.dto.PageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserAccountRepository userAccountRepository;
    private final MovieRepository movieRepository;
    private final SeriesRepository seriesRepository;

    // 리뷰 생성
    public ReviewDto createReview(ReviewDto reviewDto) {
        // 리뷰 내용이 null이거나 비어있을 경우
        if (reviewDto.getContent() == null || reviewDto.getContent().isBlank()) {
            throw new IllegalArgumentException("리뷰 내용을 입력해주세요.");
        }

        // 평점이 매겨지지 않을 경우
        if (reviewDto.getRating() == 0) {
            throw new IllegalArgumentException("평점을 매겨주세요.");
        }

        // 먼저 user를 찾아 id 저장. 없을 경우 오류 호출
        UserAccount userAccount = userAccountRepository.findById(reviewDto.getUserAccountId())
                .orElseThrow(() -> new IllegalArgumentException("찾으시려는 유저 id가 없습니다."));

        // 영화를 찾아 저장. 없을 경우 null
        Movie movie = Optional.ofNullable(reviewDto.getMovieId())
                .flatMap(movieRepository::findById)
                .orElse(null);

        // 드라마를 찾아 저장. 없을 경우 null
        Series series = Optional.ofNullable(reviewDto.getSeriesId())
                .flatMap(seriesRepository::findById)
                .orElse(null);

        // ReviewDto를 Entity로 변환해 변수에 저장
        Review review = reviewDto.toEntity(userAccount, movie, series);

        // 레포지터리에 DB 영속화 및 변수에 저장
        Review savedReview = reviewRepository.save(review);

        // 영화와 시리즈의 정보 업데이트
        updateRatingAndCount(movie, series, review.getRating(), true);

        return ReviewDto.fromEntity(savedReview);
    }

    // 모든 리뷰 조회
    public List<ReviewDto> findAllReviews() {

        // 모든 리뷰를 찾아 리턴
        return reviewRepository.findAll().stream()
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 특정 영화의 평균 평점 조회
//    public Double getMovieAverageRating(Long movieId) {
//        return reviewRepository.findAverageRatingByMovie_Id(movieId);
//    }

    // 특정 드라마의 평균 평점 조회
//    public Double getSeriesAverageRating(Long seriesId) {
//        return reviewRepository.findAverageRatingBySeries_Id(seriesId);
//    }

    // 리뷰 닉네임과 내용으로 검색
    public PageDto<ReviewDto> searchReviews(String keyword, int page, int size) {

        // Pageable 변수로 페이지와 크기를 받아 변수에 저장
        Pageable pageable = PageRequest.of(page, size);

        // 닉네임 또는 리뷰 내용에 검색어가 포함된 리뷰 조회
        Page<Review> reviewPage = reviewRepository
                .findByUserAccount_NicknameContainingOrContentContaining(keyword, keyword, pageable);

        return new PageDto<>(reviewPage.map(ReviewDto::fromEntity));
    }

    // 리뷰 수정
    public ReviewDto updateReview(Long id, ReviewDto reviewDto) {

        // id로 리뷰를 찾을 수 없을 경우
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰를 찾을 수 없습니다."));

        // 평점을 수정한다면 영화와 시리즈 정보 업데이트
        if (reviewDto.getRating() != 0 && reviewDto.getRating() != review.getRating()) {
            updateRatingAndCount(review.getMovie(), review.getSeries(), reviewDto.getRating() - review.getRating(), false);
            review.setRating(reviewDto.getRating());
        }

        // 리뷰의 평점 수정
        if (reviewDto.getRating() != 0) {
            review.setRating(reviewDto.getRating());
        }

        // 리뷰의 내용 수정
        if (reviewDto.getContent() != null && !reviewDto.getContent().isBlank()) {
            review.setContent(reviewDto.getContent());
        }

        return ReviewDto.fromEntity(review);
    }

    // 리뷰 삭제
    public void deleteReview(Long id) {

        // id로 리뷰를 찾을 수 없을 경우
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 리뷰를 찾을 수 없습니다."));

        // 영화, 시리즈 정보 업데이트
        updateRatingAndCount(review.getMovie(), review.getSeries(), -review.getRating(), true);

        reviewRepository.delete(review);
    }

    // 공통 평점 업데이트 메서드
    private void updateRatingAndCount(Movie movie, Series series, int ratingChange, boolean isAddOrDelete) {
        if (movie != null) {
            int newRatingCount = isAddOrDelete ? movie.getRatingCount() + (ratingChange > 0 ? 1 : -1) : movie.getRatingCount();
            double newAverageRating = (newRatingCount == 0) ? 0.0
                    : (movie.getAverageRating() * movie.getRatingCount() + ratingChange) / newRatingCount;
            movie.setRatingCount(newRatingCount);
            movie.setAverageRating(newAverageRating);
        }

        if (series != null) {
            int newRatingCount = isAddOrDelete ? series.getRatingCount() + (ratingChange > 0 ? 1 : -1) : series.getRatingCount();
            double newAverageRating = (newRatingCount == 0) ? 0.0
                    : (series.getAverageRating() * series.getRatingCount() + ratingChange) / newRatingCount;
            series.setRatingCount(newRatingCount);
            series.setAverageRating(newAverageRating);
        }
    }

    // 페이지네이션을 이용해서 특정 영화의 리뷰 목록을 조회
    public PageDto<ReviewDto> reviewMovieDtoPage(Long movieId, int page, int size) {

        // Pageable 변수로 페이지와 크기를 받아 변수에 저장
        Pageable pageable = PageRequest.of(page, size);

        // 영화 id로 영화를 찾아 ReviewDto 객체 목록으로 변환하여, Page 변수에 담아 return
        Page<ReviewDto> reviewDtoPage = reviewRepository.findByMovie_IdOrderByIdDesc(movieId, pageable)
                .map(ReviewDto::fromEntity);

        return new PageDto<>(reviewDtoPage);
    }

    // 페이지네이션을 이용해서 특정 드라마의 리뷰 목록을 조회
    public PageDto<ReviewDto> reviewSeriesDtoPage(Long seriesId, int page, int size) {

        // Pageable 변수로 페이지와 크기를 받아 변수에 저장
        Pageable pageable = PageRequest.of(page, size);

        // 드라마 id로 드라마를 찾아 ReviewDto 객체 목록으로 변환하여, Page 변수에 담아 return
        Page<ReviewDto> reviewDtoPage = reviewRepository.findBySeries_IdOrderByIdDesc(seriesId, pageable)
                .map(ReviewDto::fromEntity);

        return new PageDto<>(reviewDtoPage);
    }

    // 특정 영화의 총 리뷰 개수 조회
//    public long getMovieTotalCount(Long movieId) {
//        return reviewRepository.countByMovie_Id(movieId);
//    }

    // 특정 드라마의 총 리뷰 개수 조회
//    public long getSeriesTotalCount(Long seriesId) {
//        return reviewRepository.countBySeries_Id(seriesId);
//    }
}
