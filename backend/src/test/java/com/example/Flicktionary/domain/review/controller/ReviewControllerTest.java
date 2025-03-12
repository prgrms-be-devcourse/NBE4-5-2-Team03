package com.example.Flicktionary.domain.review.controller;

import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.review.dto.ReviewDto;
import com.example.Flicktionary.domain.review.repository.ReviewRepository;
import com.example.Flicktionary.domain.review.service.ReviewService;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.repository.SeriesRepository;
import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.entity.UserAccountType;
import com.example.Flicktionary.domain.user.repository.UserAccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ReviewControllerTest")
@Transactional
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private SeriesRepository seriesRepository;

    // 변수 설정
    private UserAccount testUser;
    private Movie testMovie;
    private Series testSeries;
    private ReviewDto reviewDto1;
    private ReviewDto reviewDto2;

    @BeforeEach
    void beforeEach() {

        // 테스트용 User 엔티티 생성 및 저장
        testUser = userAccountRepository.save(new UserAccount(
                null, "테스트용 유저", "test12345", "test@email.com", "테스트 유저", UserAccountType.USER
        ));

        // 테스트용 Movie 엔티티 생성
        testMovie = movieRepository.save(Movie.builder()
                .tmdbId(10000000000L)
                .title("테스트용 영화 제목")
                .overview("테스트용 영화 줄거리")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .posterPath("테스트용 이미지")
                .productionCountry("KR")
                .productionCompany("테스트용 제작사")
                .status("상영 중")
                .averageRating(4)
                .build()
        );

        // 테스트용 Series 엔티티 생성
        testSeries = seriesRepository.save(Series.builder()
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
                .build()
        );

        reviewDto1 = ReviewDto.builder()
                .userAccountId(testUser.getId())
                .nickname(testUser.getNickname())
                .movieId(testMovie.getId())
                .rating(5)
                .content("테스트용 리뷰 내용 (영화)")
                .build();

        reviewDto2 = ReviewDto.builder()
                .userAccountId(testUser.getId())
                .nickname(testUser.getNickname())
                .seriesId(testSeries.getId())
                .rating(5)
                .content("테스트용 리뷰 내용 (드라마)")
                .build();
    }

    @Test
    @DisplayName("리뷰 생성")
    void createReview() throws Exception {

        // mockMvc로 post 요청 후 Content-Type 설정과 요청 본문 설정
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto1)))
                .andExpect(status().isCreated()) // 응답 상태 검증
                .andExpect(jsonPath("$.data.content") // JSON 응답 검증
                        .value("테스트용 리뷰 내용 (영화)"));
    }

    @Test
    @DisplayName("모든 리뷰 조회")
    void getAllReviews() throws Exception {

        // 리뷰 생성
        reviewService.createReview(reviewDto1);
        reviewService.createReview(reviewDto2);

        // mockMvc로 get 요청 후 검증
        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].content")
                        .value("테스트용 리뷰 내용 (영화)"))
                .andExpect(jsonPath("$.data[1].content")
                        .value("테스트용 리뷰 내용 (드라마)"));
    }

    @Test
    @DisplayName("리뷰 수정")
    void updateReview() throws Exception {

        // 리뷰 생성
        ReviewDto savedReview = reviewService.createReview(reviewDto1);

        // 수정할 리뷰 데이터 생성
        ReviewDto modifyReview = ReviewDto.builder()
                .id(savedReview.getId())
                .userAccountId(testUser.getId())
                .nickname(testUser.getNickname())
                .movieId(testMovie.getId())
                .rating(4)
                .content("수정된 테스트용 리뷰")
                .build();

        // mockMvc로 put 요청 후 검증
        mockMvc.perform(put("/api/reviews/" + savedReview.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modifyReview)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content")
                        .value("수정된 테스트용 리뷰"))
                .andExpect(jsonPath("$.data.rating").value(4));
    }

    @Test
    @DisplayName("리뷰 삭제")
    void deleteReview() throws Exception {

        // 리뷰 생성
        ReviewDto savedReview = reviewService.createReview(reviewDto2);

        // mockMvc로 delete 요청 후 검증
        mockMvc.perform(delete("/api/reviews/" + savedReview.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("특정 영화 리뷰 페이지 조회")
    void getReviewMovie() throws Exception {

        // 리뷰 생성
        reviewService.createReview(reviewDto1);

        assertThat(testMovie).isNotNull();
        assertThat(movieRepository.findById(testMovie.getId())).isPresent();

        assertThat(reviewRepository.findAll()).isNotEmpty();

        // mockMvc로 get 요청 후 검증
        mockMvc.perform(get("/api/reviews/movie/" + testMovie.getId())
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].content")
                        .value("테스트용 리뷰 내용 (영화)"));

    }

    @Test
    @DisplayName("특정 드라마 리뷰 페이지 조회")
    void getReviewMovieSeries() throws Exception {

        // 리뷰 생성
        reviewService.createReview(reviewDto2);

        mockMvc.perform(get("/api/reviews/series/" + testSeries.getId())
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].content")
                        .value("테스트용 리뷰 내용 (드라마)"));

    }
}
