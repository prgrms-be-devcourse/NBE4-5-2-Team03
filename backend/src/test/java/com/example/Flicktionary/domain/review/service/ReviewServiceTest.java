package com.example.Flicktionary.domain.review.service;

import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.review.dto.ReviewDto;
import com.example.Flicktionary.domain.review.entity.Review;
import com.example.Flicktionary.domain.review.repository.ReviewRepository;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.repository.SeriesRepository;
import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.entity.UserAccountType;
import com.example.Flicktionary.domain.user.repository.UserAccountRepository;
import com.example.Flicktionary.global.dto.PageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

// TODO: 추가된 테스트 리팩토링
@DisplayName("리뷰 서비스 테스트")
@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private SeriesRepository seriesRepository;

    @InjectMocks
    private ReviewService reviewService;


    // 변수 설정
    private UserAccount testUser = new UserAccount(
            12L,
            "테스트용 유저",
            "test12345",
            "test@email.com",
            "테스트 유저",
            UserAccountType.USER);

    private Movie testMovie = Movie.builder()
            .id(13L)
            .tmdbId(10000000000L)
            .title("테스트용 영화 제목")
            .overview("테스트용 영화 줄거리")
            .releaseDate(LocalDate.of(2024, 1, 1))
            .posterPath("테스트용 이미지")
            .productionCountry("KR")
            .productionCompany("테스트용 제작사")
            .status("상영 중")
            .averageRating(4)
            .build();

    private Series testSeries = Series.builder()
            .id(14L)
            .tmdbId(10000000000L)
            .title("테스트용 드라마 제목")
            .plot("테스트용 드라마 줄거리")
            .episode(12)
            .status("상영중")
            .imageUrl("테스트용 이미지")
            .averageRating(4.5)
            .ratingCount(10)
            .releaseStartDate(LocalDate.of(2024, 1, 1))
            .releaseEndDate(LocalDate.of(2200, 1, 2))
            .nation("KR")
            .company("테스트용 제작사")
            .build();

    private ReviewDto reviewDto1 = ReviewDto.builder()
            .userAccountId(testUser.getId())
            .nickname(testUser.getNickname())
            .movieId(testMovie.getId())
            .rating(5)
            .content("테스트용 리뷰 내용 (영화)")
            .build();

    private ReviewDto reviewDto2 = ReviewDto.builder()
            .userAccountId(testUser.getId())
            .nickname(testUser.getNickname())
            .seriesId(testSeries.getId())
            .rating(5)
            .content("테스트용 리뷰 내용 (드라마)")
            .build();

    @Test
    @DisplayName("리뷰 작성")
    void printReview() {
        given(userAccountRepository.findById(reviewDto1.getUserAccountId())).willReturn(Optional.of(testUser));
        given(movieRepository.findById(reviewDto1.getMovieId())).willReturn(Optional.of(testMovie));
        given(reviewRepository.save(any(Review.class))).willReturn(Review.builder()
                .id(1L)
                .userAccount(testUser)
                .movie(testMovie)
                .rating(reviewDto1.getRating())
                .content(reviewDto1.getContent())
                .build());

        // 리뷰 생성 및 변수에 저장
        ReviewDto reviewDto = reviewService.createReview(reviewDto1);

        /// 검증 ///
        assertThat(reviewDto).isNotNull();
        assertThat(reviewDto.getMovieId()).isNotNull();
        assertThat(reviewDto.getSeriesId()).isNull();
        assertEquals(1L, reviewDto.getId());
        assertEquals(reviewDto1.getRating(), reviewDto.getRating());
        assertEquals(reviewDto1.getContent(), reviewDto.getContent());

    }

    @Test
    @DisplayName("리뷰 작성 실패(리뷰 내용이 null이거나 빈 문자열일 때 예외 발생)")
    void printReviewFailNull() {
        // 리뷰 내용이 비어있는 리뷰 생성
        ReviewDto reviewEmpty = ReviewDto.builder()
                .userAccountId(testUser.getId())
                .nickname(testUser.getNickname())
                .movieId(testMovie.getId())
                .rating(5)
                .content("")
                .build();

        // 리뷰 내용이 null인 리뷰 생성
        ReviewDto reviewNull = ReviewDto.builder()
                .userAccountId(testUser.getId())
                .nickname(testUser.getNickname())
                .movieId(testMovie.getId())
                .rating(5)
                .content(null)
                .build();

        Throwable thrownOnEmpty = catchThrowable(() -> reviewService.createReview(reviewEmpty));
        Throwable thrownOnNull = catchThrowable(() -> reviewService.createReview(reviewNull));

        /// 검증 ///
        assertThat(thrownOnEmpty)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 내용을 입력해주세요.");
        assertThat(thrownOnNull)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 내용을 입력해주세요.");
    }

    @Test
    @DisplayName("리뷰 작성 실패(평점이 없을 때 예외 발생)")
    void printReviewFailRating() {
        // 평점이 없는 리뷰 생성
        ReviewDto reviewNoRating = ReviewDto.builder()
                .userAccountId(testUser.getId())
                .nickname(testUser.getNickname())
                .movieId(testMovie.getId())
                .rating(0)
                .content("테스트용 리뷰 내용")
                .build();

        Throwable thrown = catchThrowable(() -> reviewService.createReview(reviewNoRating));

        /// 검증 ///
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평점을 매겨주세요.");
    }

    /**
    @Test
    @DisplayName("리뷰 작성 시 영화 정보 업데이트")
    void createReviewUpdateMovie() {
        int ratingCount = testMovie.getRatingCount();
        double averageRating = testMovie.getAverageRating();
        long id = testMovie.getId();

        // 리뷰 생성 및 변수에 저장
        ReviewDto review = reviewService.createReview(reviewDto1);

        int newRatingCount = ratingCount + 1;
        double newAverageRating = (averageRating * ratingCount + review.getRating()) / newRatingCount;
        Movie updatedMovie = movieRepository.findById(id).get();

        assertThat(updatedMovie.getRatingCount()).isEqualTo(newRatingCount);
        assertThat(updatedMovie.getAverageRating()).isEqualTo(newAverageRating);
    }

    @Test
    @DisplayName("리뷰 작성 시 시리즈 정보 업데이트")
    void createReviewUpdateSeries() {
        int ratingCount = testSeries.getRatingCount();
        double averageRating = testSeries.getAverageRating();
        long id = testSeries.getId();

        // 리뷰 생성 및 변수에 저장
        ReviewDto review = reviewService.createReview(reviewDto2);

        int newRatingCount = ratingCount + 1;
        double newAverageRating = (averageRating * ratingCount + review.getRating()) / newRatingCount;
        Series updatedSeries = seriesRepository.findById(id).get();

        assertThat(updatedSeries.getRatingCount()).isEqualTo(newRatingCount);
        assertThat(updatedSeries.getAverageRating()).isEqualTo(newAverageRating);
    }

    */
    @Test
    @DisplayName("모든 리뷰 조회")
    void printAllReviews() {
        given(reviewRepository.findAll())
                .willReturn(List.of(reviewDto1.toEntity(testUser, testMovie, null),
                        reviewDto2.toEntity(testUser, null, testSeries)));

        // 모든 리뷰 List 변수에 저장
        List<ReviewDto> reviews = reviewService.findAllReviews();

        /// 검증 ///
        assertThat(reviews).isNotEmpty().hasSize(2);
        assertThat(reviews).flatExtracting(ReviewDto::getContent)
                .contains(reviewDto1.getContent(), reviewDto2.getContent());
        assertThat(reviews).flatExtracting(ReviewDto::getMovieId).contains(reviewDto1.getMovieId());
        assertThat(reviews).flatExtracting(ReviewDto::getSeriesId).contains(reviewDto2.getSeriesId());
        then(reviewRepository).should().findAll();
    }
    // TODO: 변경된 쿼리 메소트에 맞춰 테스트 수정
    /**
    @Test
    @DisplayName("특정 영화의 리뷰 조회")
    void printReviewByMovie() {
        given(reviewRepository.findByMovie_Id(any(Long.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(
                        List.of(reviewDto1.toEntity(testUser, testMovie, null)),
                        PageRequest.of(0, 5),
                        1L)
                );

        // 영화 id로 영화를 찾아 리뷰들을 PageDto 변수에 저장
        PageDto<ReviewDto> reviewDtoPageDto = reviewService.reviewMovieDtoPage(reviewDto1.getMovieId(), 0, 5);

        /// 검증 ///
        assertThat(reviewDtoPageDto).isNotNull();
        assertThat(reviewDtoPageDto.getItems()).hasSize(1);
        assertThat(reviewDtoPageDto.getItems()).flatExtracting(ReviewDto::getMovieId)
                .contains(reviewDto1.getMovieId());
        assertEquals(1, reviewDtoPageDto.getCurPageNo());
        assertEquals(5, reviewDtoPageDto.getPageSize());
        then(reviewRepository).should().findByMovie_Id(any(Long.class), any(Pageable.class));
    }

    @Test
    @DisplayName("특정 드라마의 리뷰 조회")
    void printReviewBySeries() {
        given(reviewRepository.findBySeries_Id(any(Long.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(
                        List.of(reviewDto2.toEntity(testUser, null, testSeries)),
                        PageRequest.of(0, 5),
                        1L)
                );

        // 영화 id로 영화를 찾아 리뷰들을 PageDto 변수에 저장
        PageDto<ReviewDto> reviewDtoPageDto = reviewService.reviewSeriesDtoPage(reviewDto2.getSeriesId(), 0, 5);

        /// 검증 ///
        assertThat(reviewDtoPageDto).isNotNull();
        assertThat(reviewDtoPageDto.getItems()).hasSize(1);
        assertThat(reviewDtoPageDto.getItems()).flatExtracting(ReviewDto::getSeriesId)
                .contains(reviewDto2.getSeriesId());
        assertEquals(1, reviewDtoPageDto.getCurPageNo());
        assertEquals(5, reviewDtoPageDto.getPageSize());
        then(reviewRepository).should().findBySeries_Id(any(Long.class), any(Pageable.class));
    }
    */
    @DisplayName("리뷰 수정")
    @Test
    // TODO: 새로운 평점/내용 검증 로직까지 테스트하는 것을 검토
    void updateReview() {
        given(reviewRepository.findById(reviewDto1.getId()))
                .willReturn(Optional.of(reviewDto1.toEntity(testUser, testMovie, null)));

        // 수정할 리뷰 내용 변수에 저장
        ReviewDto updatedReviewDto = ReviewDto.builder()
                .id(reviewDto1.getId())
                .userAccountId(reviewDto1.getUserAccountId())
                .nickname(reviewDto1.getNickname())
                .movieId(reviewDto1.getMovieId())
                .rating(4)
                .content("(테스트)수정된 리뷰 내용")
                .build();

        // 수정
        ReviewDto result = reviewService.updateReview(reviewDto1.getId(), updatedReviewDto);

        /// 검증 ///
        assertThat(result).isNotNull();
        assertEquals(4, result.getRating());
        assertEquals("(테스트)수정된 리뷰 내용", result.getContent());
        then(reviewRepository).should().findById(reviewDto1.getId());
    }
    /**
    @DisplayName("존재하지 않는 리뷰 수정")
    @Test
    @DisplayName("리뷰 수정 시 영화 정보 업데이트")
    void updateReviewUpdateMovie() {
        // 리뷰 생성
        ReviewDto savedReview = reviewService.createReview(reviewDto1);

        // 수정할 리뷰 내용 변수에 저장
        ReviewDto updatedReviewDto = ReviewDto.builder()
                .id(savedReview.getId())
                .userAccountId(savedReview.getUserAccountId())
                .nickname(savedReview.getNickname())
                .movieId(savedReview.getMovieId())
                .rating(4)
                .content("(테스트)수정된 리뷰 내용")
                .build();

        int ratingCount = testMovie.getRatingCount();
        double averageRating = testMovie.getAverageRating();
        long id = testMovie.getId();

        // 수정
        ReviewDto review = reviewService.updateReview(savedReview.getId(), updatedReviewDto);

        double newAverageRating = (averageRating * ratingCount - savedReview.getRating() + review.getRating()) / ratingCount;
        Movie updatedMovie = movieRepository.findById(id).get();

        assertThat(updatedMovie.getRatingCount()).isEqualTo(ratingCount);
        assertThat(updatedMovie.getAverageRating()).isEqualTo(newAverageRating);
    }

    @Test
    @DisplayName("리뷰 작성 시 시리즈 정보 업데이트")
    void updateReviewUpdateSeries() {
        // 리뷰 생성
        ReviewDto savedReview = reviewService.createReview(reviewDto2);

        // 수정할 리뷰 내용 변수에 저장
        ReviewDto updatedReviewDto = ReviewDto.builder()
                .id(savedReview.getId())
                .userAccountId(savedReview.getUserAccountId())
                .nickname(savedReview.getNickname())
                .seriesId(savedReview.getSeriesId())
                .rating(4)
                .content("(테스트)수정된 리뷰 내용")
                .build();

        int ratingCount = testSeries.getRatingCount();
        double averageRating = testSeries.getAverageRating();
        long id = testSeries.getId();

        // 수정
        ReviewDto review = reviewService.updateReview(savedReview.getId(), updatedReviewDto);

        double newAverageRating = (averageRating * ratingCount - savedReview.getRating() + review.getRating()) / ratingCount;
        Series updatedSeries = seriesRepository.findById(id).get();

        assertThat(updatedSeries.getRatingCount()).isEqualTo(ratingCount);
        assertThat(updatedSeries.getAverageRating()).isEqualTo(newAverageRating);
    }

    */
    @DisplayName("존재하지 않는 리뷰 수정")
    @Test
    void updateNonexistentReview() {
        given(reviewRepository.findById(1234L)).willReturn(Optional.empty());


        // 수정
        Throwable thrown = catchThrowable(() -> reviewService.updateReview(1234L, reviewDto1));

        /// 검증 ///
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 리뷰를 찾을 수 없습니다.");
        then(reviewRepository).should().findById(1234L);
    }

    @DisplayName("리뷰 삭제")
    @Test
    void deleteReview() {
        given(reviewRepository.findById(reviewDto1.getId()))
                .willReturn(Optional.of(reviewDto1.toEntity(testUser, testMovie, null)));

        // 리뷰 삭제
        reviewService.deleteReview(reviewDto1.getId());

        /// 검증 ///
        then(reviewRepository).should().findById(reviewDto1.getId());
        then(reviewRepository).should().delete(any(Review.class));
    }
    /**
    @DisplayName("존재하지 않는 리뷰 삭제")
    @Test
    @DisplayName("리뷰 수정 시 영화 정보 업데이트")
    void deleteReviewUpdateMovie() {
        // 리뷰 생성
        ReviewDto savedReview = reviewService.createReview(reviewDto1);

        int ratingCount = testMovie.getRatingCount();
        double averageRating = testMovie.getAverageRating();
        long id = testMovie.getId();

        // 리뷰 삭제
        reviewService.deleteReview(savedReview.getId());

        int newRatingCount = ratingCount - 1;
        double newAverageRating = (averageRating * ratingCount - savedReview.getRating()) / newRatingCount;
        Movie updatedMovie = movieRepository.findById(id).get();

        assertThat(updatedMovie.getRatingCount()).isEqualTo(newRatingCount);
        assertThat(updatedMovie.getAverageRating()).isEqualTo(newAverageRating);
    }

    @Test
    @DisplayName("리뷰 삭제 시 시리즈 정보 업데이트")
    void deleteReviewUpdateSeries() {
        // 리뷰 생성
        ReviewDto savedReview = reviewService.createReview(reviewDto2);

        int ratingCount = testSeries.getRatingCount();
        double averageRating = testSeries.getAverageRating();
        long id = testSeries.getId();

        // 리뷰 삭제
        reviewService.deleteReview(savedReview.getId());

        int newRatingCount = ratingCount - 1;
        double newAverageRating = (averageRating * ratingCount - savedReview.getRating()) / newRatingCount;
        Series updatedSeries = seriesRepository.findById(id).get();

        assertThat(updatedSeries.getRatingCount()).isEqualTo(newRatingCount);
        assertThat(updatedSeries.getAverageRating()).isEqualTo(newAverageRating);
    }

    */
    @DisplayName("존재하지 않는 리뷰 삭제")
    @Test
    void deleteNonexistentReview() {
        given(reviewRepository.findById(reviewDto1.getId())).willReturn(Optional.empty());

        // 리뷰 삭제
        Throwable thrown = catchThrowable(() -> reviewService.deleteReview(reviewDto1.getId()));

        /// 검증 ///
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("해당 리뷰를 찾을 수 없습니다.");
    }
}
