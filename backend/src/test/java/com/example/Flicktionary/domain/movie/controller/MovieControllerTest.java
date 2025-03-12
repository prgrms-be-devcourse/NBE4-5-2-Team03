package com.example.Flicktionary.domain.movie.controller;

import com.example.Flicktionary.domain.movie.dto.MovieResponse;
import com.example.Flicktionary.domain.movie.dto.MovieResponseWithDetail;
import com.example.Flicktionary.domain.movie.service.MovieService;
import com.example.Flicktionary.global.dto.PageDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MovieControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private MovieService movieService;

    @Test
    @DisplayName("영화 목록 조회 - 성공")
    void getMovies1() throws Exception {
        String keyword = "";
        int page = 1;
        int pageSize = 10;
        String sortBy = "id";

        ResultActions resultActions = mvc.perform(get("/api/movies")
                        .param("keyword", keyword)
                        .param("page", "%d".formatted(page))
                        .param("pageSize", "%d".formatted(pageSize))
                        .param("sortBy", sortBy)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        PageDto<MovieResponse> result = movieService.getMovies(keyword, page, pageSize, sortBy);

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(MovieController.class))
                .andExpect(handler().methodName("getMovies"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].id").value(result.getItems().getFirst().getId()))
                .andExpect(jsonPath("$.data.items[1].id").value(result.getItems().get(1).getId()))
                .andExpect(jsonPath("$.data.totalPages").value(result.getTotalPages()))
                .andExpect(jsonPath("$.data.totalItems").value(result.getTotalItems()));
    }

    @Test
    @DisplayName("영화 목록 조회 - 실패 - 잘못된 정렬 기준")
    void getMovies2() throws Exception {
        String keyword = "";
        int page = 1;
        int pageSize = 10;
        String sortBy = "Unknown";

        ResultActions resultActions = mvc.perform(get("/api/movies")
                        .param("keyword", keyword)
                        .param("page", "%d".formatted(page))
                        .param("pageSize", "%d".formatted(pageSize))
                        .param("sortBy", sortBy)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(MovieController.class))
                .andExpect(handler().methodName("getMovies"));
    }

    @Test
    @DisplayName("영화 상세 조회 - 성공")
    void getMovie1() throws Exception {
        long id = 1L;

        ResultActions resultActions = mvc.perform(get("/api/movies/%d".formatted(id))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        MovieResponseWithDetail result = movieService.getMovie(id);

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(MovieController.class))
                .andExpect(handler().methodName("getMovie"))
                .andExpect(jsonPath("$.data.id").value(result.getId()))
                .andExpect(jsonPath("$.data.tmdbId").value(result.getTmdbId()))
                .andExpect(jsonPath("$.data.title").value(result.getTitle()))
                .andExpect(jsonPath("$.data.casts[0].characterName").value(result.getCasts().getFirst().getCharacterName()))
                .andExpect(jsonPath("$.data.genres[0].id").value(result.getGenres().getFirst().getId()));
    }

    @Test
    @DisplayName("영화 상세 조회 - 실패 - 없는 영화 조회")
    void getMovie2() throws Exception {
        long id = 1000000000000000000L;

        ResultActions resultActions = mvc.perform(get("/api/movies/%d".formatted(id))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(handler().handlerType(MovieController.class))
                .andExpect(handler().methodName("getMovie"));
    }
}