package com.example.Flicktionary.domain.review.service;

import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.review.dto.ReviewDto;
import com.example.Flicktionary.domain.review.entity.Review;
import com.example.Flicktionary.domain.review.repository.ReviewRepository;
import com.example.Flicktionary.domain.series.domain.Series;
import com.example.Flicktionary.domain.series.repository.SeriesRepository;
import com.example.Flicktionary.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final SeriesRepository seriesRepository;

    // 리뷰 생성
    public ReviewDto createReview(ReviewDto reviewDto) {

        // 먼저 user를 찾아 id 저장. 없을 경우 오류 호출
        User user = userRepository.findById(reviewDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("찾으시려는 유저 id가 없습니다."));

        // 영화를 찾아 저장. 없을 경우 null
        Movie movie = Optional.ofNullable(reviewDto.getMovieId())
                .map(movieRepository::findById)
                .flatMap(m -> m)
                .orElse(null);

        // 드라마를 찾아 저장. 없을 경우 null
        Series series = Optional.ofNullable(reviewDto.getSeriesId())
                .map(seriesRepository::findById)
                .flatMap(s -> s)
                .orElse(null);

        // 리뷰 내용이 null이거나 비어있을 경우
        if (reviewDto.getContent() == null || reviewDto.getContent().isBlank()) {
            throw new IllegalArgumentException("리뷰 내용을 입력해주세요.");
        }

        // 평점이 매겨지지 않을 경우
        if (reviewDto.getRating() == 0) {
            throw new IllegalArgumentException("평점을 매겨주세요.");
        }

        // ReviewDto를 Entity로 변환해 변수에 저장
        Review review = reviewDto.toEntity(user, movie, series);

        // 레포지터리에 DB 영속화 및 변수에 저장
        Review savedReview = reviewRepository.save(review);

        return ReviewDto.fromEntity(savedReview);
    }

    // 모든 리뷰 조회
    public List<ReviewDto> findAllReviews() {

        // findAll()이 Iterable을 반환할 수 없어, List로 변환시켜 변수에 저장
        List<Review> reviews = new ArrayList<>();

        reviewRepository.findAll().forEach(reviews::add);

        // 스트림을 사용해 Entity를 Dto로 변환
        return reviews.stream()
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 리뷰 수정
    public ReviewDto updateReview(Long id, ReviewDto reviewDto) {

        // id로 리뷰를 찾을 수 없을 경우
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰를 찾을 수 없습니다."));

        // 리뷰의 평점이나 내용 수정
        review.setRating(reviewDto.getRating());
        review.setContent(reviewDto.getContent());

        // DB에 영속화 및 리턴
        Review savedReview = reviewRepository.save(review);
        return ReviewDto.fromEntity(savedReview);
    }

    // 리뷰 삭제
    public void deleteReview(Long id) {

        // id로 리뷰를 찾을 수 없을 경우
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 리뷰를 찾을 수 없습니다."));

        reviewRepository.delete(review);
    }

    // 페이지네이션을 이용해서 특정 영화의 리뷰 목록을 조회
    public Page<ReviewDto> reviewMovieDtoPage(Long movie_id, int page, int size) {

        // Pageable 변수로 페이지와 크기를 받아 변수에 저장
        Pageable pageable = PageRequest.of(page, size);

        return reviewRepository.findByMovieId(movie_id, pageable).map(ReviewDto::fromEntity);
    }

    // 페이지네이션을 이용해서 특정 드라마의 리뷰 목록을 조회
    public Page<ReviewDto> reviewSeriesDtoPage(Long series_id, int page, int size) {

        // Pageable 변수로 페이지와 크기를 받아 변수에 저장
        Pageable pageable = PageRequest.of(page, size);

        return reviewRepository.findBySeriesId(series_id, pageable).map(ReviewDto::fromEntity);
    }
}
