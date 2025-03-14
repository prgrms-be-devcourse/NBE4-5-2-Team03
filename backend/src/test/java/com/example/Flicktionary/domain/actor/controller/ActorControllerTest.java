package com.example.Flicktionary.domain.actor.controller;

import com.example.Flicktionary.domain.actor.entity.Actor;
import com.example.Flicktionary.domain.actor.service.ActorService;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.series.entity.Series;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ActorControllerTest {
    private MockMvc mockMvc;
    @Mock
    private ActorService actorService;
    @InjectMocks
    private ActorController actorController;

    private Actor testActor;
    private Movie testMovie;
    private Series testSeries;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(actorController).build();

        testActor = new Actor(1L, "actor", "actor.png");
        testMovie = Movie.builder()
                .id(1L)
                .tmdbId(1L)
                .title("movie")
                .posterPath("movie.png")
                .releaseDate(LocalDate.of(2022, 1, 1))
                .build();
        testSeries = Series.builder()
                .id(1L)
                .tmdbId(1L)
                .title("series")
                .imageUrl("series.png")
                .releaseStartDate(LocalDate.of(2022, 1, 1))
                .releaseEndDate(LocalDate.of(2023, 1, 1))
                .build();
    }
    
    @Test
    @DisplayName("배우 상세 조회 - 성공")
    void getActor1() throws Exception {
        // Given
        given(actorService.getActorById(1L)).willReturn(Optional.of(testActor));
        given(actorService.getMoviesByActorId(1L)).willReturn(List.of(testMovie));
        given(actorService.getSeriesByActorId(1L)).willReturn(List.of(testSeries));

        // When & Then
        mockMvc.perform(get("/api/actors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("actor"))
                .andExpect(jsonPath("$.data.profilePath").value("actor.png"))
                .andExpect(jsonPath("$.data.movies[0].title").value("movie"))
                .andExpect(jsonPath("$.data.series[0].title").value("series"));
    }

    @Test
    @DisplayName("배우 상세 조회 - 실패 - 없는 배우 조회")
    void getActor2() throws Exception {
        // Given
        given(actorService.getActorById(999L)).willReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/actors/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.message").value("999번 배우가 없습니다."));
    }

    /**
     * ✅ 배우 목록 조회 API 테스트 (성공)
     */
    @Test
    @DisplayName("배우 목록 조회 - 성공")
    void getActors() throws Exception {
        // Given
        int page = 1, pageSize = 10;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Actor> actorPage = new PageImpl<>(List.of(testActor), pageable, 1);

        given(actorService.getActors(any(String.class), eq(page), eq(pageSize))).willReturn(actorPage);

        // When & Then
        mockMvc.perform(get("/api/actors")
                        .param("keyword", "test")
                        .param("page", String.valueOf(page))
                        .param("pageSize", String.valueOf(pageSize)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("actor"));
    }
}