package com.example.Flicktionary.domain.review.controller;

import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.review.dto.ReviewDto;
import com.example.Flicktionary.domain.review.service.ReviewService;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.entity.UserAccountType;
import com.example.Flicktionary.domain.user.service.UserAccountJwtAuthenticationService;
import com.example.Flicktionary.domain.user.service.UserAccountService;
import com.example.Flicktionary.global.dto.PageDto;
import com.example.Flicktionary.global.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("리뷰 도메인 컨트롤러 테스트")
@Import({ReviewService.class,
        UserAccountService.class,
        UserAccountJwtAuthenticationService.class,
        CustomUserDetailsService.class})
@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private UserAccountService userAccountService;

    @MockitoBean
    private UserAccountJwtAuthenticationService userAccountJwtAuthenticationService;

    // 변수 설정
    private UserAccount testUser = new UserAccount(
            null, "테스트용 유저", "test12345", "test@email.com", "테스트 유저", UserAccountType.USER
    );

    private Movie testMovie = Movie.builder()
            .id(123L)
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
            .id(321L)
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
            .id(123L)
            .userAccountId(testUser.getId())
            .nickname(testUser.getNickname())
            .movieId(testMovie.getId())
            .rating(5)
            .content("테스트용 리뷰 내용 (영화)")
            .build();

    private ReviewDto reviewDto2 = ReviewDto.builder()
            .id(321L)
            .userAccountId(testUser.getId())
            .nickname(testUser.getNickname())
            .seriesId(testSeries.getId())
            .rating(5)
            .content("테스트용 리뷰 내용 (드라마)")
            .build();

    @Test
    @DisplayName("리뷰 생성")
    void createReview() throws Exception {
        given(reviewService.createReview(any(ReviewDto.class))).willReturn(reviewDto1);

        // mockMvc로 post 요청 후 Content-Type 설정과 요청 본문 설정
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto1)))
                .andExpect(status().isCreated()) // 응답 상태 검증
                .andExpect(jsonPath("$.data.content") // JSON 응답 검증
                        .value("테스트용 리뷰 내용 (영화)"));

        then(reviewService).should().createReview(any(ReviewDto.class));
    }

    @Test
    @DisplayName("모든 리뷰 조회")
    void getAllReviews() throws Exception {
        given(reviewService.findAllReviews()).willReturn(List.of(reviewDto1, reviewDto2));

        // mockMvc로 get 요청 후 검증
        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].content")
                        .value("테스트용 리뷰 내용 (영화)"))
                .andExpect(jsonPath("$.data[1].content")
                        .value("테스트용 리뷰 내용 (드라마)"));

        then(reviewService).should().findAllReviews();
    }

    @Test
    @DisplayName("리뷰 수정")
    void updateReview() throws Exception {
        ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<ReviewDto> reviewDtoCaptor = ArgumentCaptor.forClass(ReviewDto.class);
        // 수정할 리뷰 데이터 생성
        ReviewDto modifyReview = ReviewDto.builder()
                .id(reviewDto1.getId())
                .userAccountId(testUser.getId())
                .nickname(testUser.getNickname())
                .movieId(testMovie.getId())
                .rating(4)
                .content("수정된 테스트용 리뷰")
                .build();
        given(reviewService.updateReview(longCaptor.capture(), reviewDtoCaptor.capture()))
                .willReturn(modifyReview);

        // mockMvc로 put 요청 후 검증
        mockMvc.perform(put("/api/reviews/" + reviewDto1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modifyReview)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content")
                        .value("수정된 테스트용 리뷰"))
                .andExpect(jsonPath("$.data.rating").value(4));
        ReviewDto captured = reviewDtoCaptor.getValue();

        assertEquals(reviewDto1.getId(), longCaptor.getValue());
        assertEquals(modifyReview.getId(), captured.getId());
        assertEquals(modifyReview.getContent(), captured.getContent());
        assertEquals(modifyReview.getRating(), captured.getRating());
        then(reviewService).should().updateReview(any(Long.class), any(ReviewDto.class));
    }

    @Test
    @DisplayName("리뷰 삭제")
    void deleteReview() throws Exception {
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        doNothing().when(reviewService).deleteReview(captor.capture());

        // mockMvc로 delete 요청 후 검증
        mockMvc.perform(delete("/api/reviews/" + reviewDto1.getId()))
                .andExpect(status().isNoContent());
        Long captured = captor.getValue();

        assertEquals(reviewDto1.getId(), captured);
        then(reviewService).should().deleteReview(reviewDto1.getId());
    }

    @Test
    @DisplayName("특정 영화 리뷰 페이지 조회")
    void getReviewMovie() throws Exception {
        ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> integerCaptor = ArgumentCaptor.forClass(Integer.class);
        given(reviewService.reviewMovieDtoPage(
                longCaptor.capture(),
                integerCaptor.capture(),
                integerCaptor.capture()))
                .willReturn(new PageDto<>(new PageImpl<>(
                        List.of(reviewDto1),
                        PageRequest.of(0, 5),
                        10)
                ));

        // mockMvc로 get 요청 후 검증
        mockMvc.perform(get("/api/reviews/movie/" + reviewDto1.getMovieId())
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].content")
                        .value("테스트용 리뷰 내용 (영화)"));
        List<Integer> integerArgs = integerCaptor.getAllValues();

        assertEquals(reviewDto1.getId(), longCaptor.getValue());
        assertEquals(0, integerArgs.getFirst());
        assertEquals(5, integerArgs.get(1));
        then(reviewService).should()
                .reviewMovieDtoPage(any(Long.class), any(Integer.class), any(Integer.class));
    }

    @Test
    @DisplayName("특정 드라마 리뷰 페이지 조회")
    void getReviewMovieSeries() throws Exception {
        ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> integerCaptor = ArgumentCaptor.forClass(Integer.class);
        given(reviewService.reviewSeriesDtoPage(
                longCaptor.capture(),
                integerCaptor.capture(),
                integerCaptor.capture()))
                .willReturn(new PageDto<>(new PageImpl<>(
                        List.of(reviewDto2),
                        PageRequest.of(0, 5),
                        10)
                ));

        // mockMvc로 get 요청 후 검증
        mockMvc.perform(get("/api/reviews/series/" + reviewDto2.getSeriesId())
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].content")
                        .value("테스트용 리뷰 내용 (드라마)"));
        List<Integer> integerArgs = integerCaptor.getAllValues();

        assertEquals(reviewDto2.getId(), longCaptor.getValue());
        assertEquals(0, integerArgs.getFirst());
        assertEquals(5, integerArgs.get(1));
        then(reviewService).should()
                .reviewSeriesDtoPage(any(Long.class), any(Integer.class), any(Integer.class));
    }
}
