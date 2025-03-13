package com.example.Flicktionary.domain.movie.controller;

import com.example.Flicktionary.domain.movie.dto.MovieResponse;
import com.example.Flicktionary.domain.movie.dto.MovieResponseWithDetail;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.service.MovieService;
import com.example.Flicktionary.domain.user.service.UserAccountJwtAuthenticationService;
import com.example.Flicktionary.domain.user.service.UserAccountService;
import com.example.Flicktionary.global.dto.PageDto;
import com.example.Flicktionary.global.security.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("영화 도메인 컨트롤러 테스트")
@Import({MovieService.class,
        UserAccountService.class,
        UserAccountJwtAuthenticationService.class,
        CustomUserDetailsService.class})
@WebMvcTest(MovieController.class)
@AutoConfigureMockMvc(addFilters = false)
class MovieControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private UserAccountService userAccountService;

    @MockitoBean
    private UserAccountJwtAuthenticationService userAccountJwtAuthenticationService;

    private Movie testMovie1 = Movie.builder()
            .id(123L)
            .tmdbId(321L)
            .title("testTitle1")
            .averageRating(1.23)
            .ratingCount(123)
            .build();

    private Movie testMovie2 = Movie.builder()
            .id(456L)
            .tmdbId(654L)
            .title("testTitle2")
            .averageRating(4.56)
            .ratingCount(456)
            .build();

    @Test
    @DisplayName("영화 목록 조회 - 성공")
    void getMovies1() throws Exception {
        String keyword = "", sortBy = "id";
        int page = 1, pageSize = 10;
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> integerCaptor = ArgumentCaptor.forClass(Integer.class);
        given(movieService.getMovies(
                stringCaptor.capture(),
                integerCaptor.capture(),
                integerCaptor.capture(),
                stringCaptor.capture())
        ).willReturn(new PageDto<>(new PageImpl<>(
                List.of(new MovieResponse(testMovie1), new MovieResponse(testMovie2)),
                PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, "id")),
                10)));

        ResultActions resultActions = mvc.perform(get("/api/movies")
                        .param("keyword", keyword)
                        .param("page", "%d".formatted(page))
                        .param("pageSize", "%d".formatted(pageSize))
                        .param("sortBy", sortBy)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(MovieController.class))
                .andExpect(handler().methodName("getMovies"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].id").value(testMovie1.getId()))
                .andExpect(jsonPath("$.data.items[1].id").value(testMovie2.getId()))
                .andExpect(jsonPath("$.data.totalPages").value(10 / pageSize))
                .andExpect(jsonPath("$.data.totalItems").value(10));
        List<String> stringArgs = stringCaptor.getAllValues();
        List<Integer> integerArgs = integerCaptor.getAllValues();

        assertEquals(keyword, stringArgs.getFirst());
        assertEquals(page, integerArgs.getFirst());
        assertEquals(pageSize, integerArgs.get(1));
        assertEquals(sortBy, stringArgs.get(1));
        then(movieService).should().getMovies(keyword, page, pageSize, sortBy);
    }

    @Test
    @DisplayName("영화 목록 조회 - 실패 - 잘못된 정렬 기준")
    void getMovies2() throws Exception {
        given(movieService.getMovies(any(String.class), any(Integer.class), any(Integer.class), any(String.class)))
                .willThrow(new RuntimeException("잘못된 정렬기준입니다."));

        ResultActions resultActions = mvc.perform(get("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(MovieController.class))
                .andExpect(handler().methodName("getMovies"))
                .andExpect(jsonPath("$.message").value("잘못된 정렬기준입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("영화 상세 조회 - 성공")
    void getMovie1() throws Exception {
        given(movieService.getMovie(testMovie1.getId()))
                .willReturn(new MovieResponseWithDetail(testMovie1));

        ResultActions resultActions = mvc.perform(get("/api/movies/%d".formatted(testMovie1.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(MovieController.class))
                .andExpect(handler().methodName("getMovie"))
                .andExpect(jsonPath("$.data.id").value(testMovie1.getId()))
                .andExpect(jsonPath("$.data.tmdbId").value(testMovie1.getTmdbId()))
                .andExpect(jsonPath("$.data.title").value(testMovie1.getTitle()))
                // TODO: 배우와 장르 정보가 있는 영화 엔티티를 작성해 테스트를 실행할 것
                .andExpect(jsonPath("$.data.casts").isEmpty())
                .andExpect(jsonPath("$.data.genres").isEmpty());
        then(movieService).should().getMovie(testMovie1.getId());
    }

    @Test
    @DisplayName("영화 상세 조회 - 실패 - 없는 영화 조회")
    void getMovie2() throws Exception {
        long id = 1000000000000000000L;
        given(movieService.getMovie(id)).willThrow(
                new NoSuchElementException("%d번 영화를 찾을 수 없습니다.".formatted(id))
        );

        ResultActions resultActions = mvc.perform(get("/api/movies/%d".formatted(id))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(handler().handlerType(MovieController.class))
                .andExpect(handler().methodName("getMovie"))
                .andExpect(jsonPath("$.message").value("%d번 영화를 찾을 수 없습니다.".formatted(id)))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}