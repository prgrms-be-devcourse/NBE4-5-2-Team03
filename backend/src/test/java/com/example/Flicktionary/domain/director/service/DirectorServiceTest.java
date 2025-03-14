package com.example.Flicktionary.domain.director.service;

import com.example.Flicktionary.domain.director.entity.Director;
import com.example.Flicktionary.domain.director.repository.DirectorRepository;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.repository.SeriesRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectorServiceTest {
    @Mock
    private DirectorRepository directorRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private SeriesRepository seriesRepository;

    @InjectMocks
    private DirectorService directorService;

    private Director director;
    private Movie movie;
    private Series series;

    @BeforeEach
    void setUp() {
        director = new Director(1L, "director", "director.png");
        movie = Movie.builder()
                .id(1L)
                .tmdbId(1L)
                .title("movie")
                .posterPath("movie.png")
                .releaseDate(LocalDate.of(2022, 1, 1))
                .director(director)
                .build();
        series = Series.builder()
                .id(1L)
                .tmdbId(1L)
                .title("series")
                .imageUrl("series.png")
                .releaseStartDate(LocalDate.of(2022, 1, 1))
                .releaseEndDate(LocalDate.of(2023, 1, 1))
                .director(director)
                .build();
    }

    @Test
    @DisplayName("감독 목록 조회 - 성공")
    void getDirectors() {
        // Given
        String keyword = "";
        int page = 1, pageSize = 10;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Director> directorPage = new PageImpl<>(List.of(director), pageable, 1);

        given(directorRepository.findByNameLike(any(String.class), eq(pageable))).willReturn(directorPage);

        // When
        Page<Director> result = directorService.getDirectors(keyword, page, pageSize);

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertEquals(1, result.getContent().size());
        assertEquals("director", result.getContent().get(0).getName());

        then(directorRepository).should().findByNameLike(any(String.class), eq(pageable));
    }

    @Test
    @DisplayName("감독 상세 조회 - 성공")
    void getDirector() {
        // Given
        given(directorRepository.findById(1L)).willReturn(Optional.of(director));

        // When
        Optional<Director> result = directorService.getDirector(1L);

        // Then
        assertThat(result).isPresent();
        assertEquals(director, result.get());

        then(directorRepository).should().findById(1L);
    }

    @Test
    @DisplayName("감독 아이디로 영화 조회 - 성공")
    void getMoviesByDirectorId() {
        // Arrange
        when(movieRepository.findByDirectorId(director.getId())).thenReturn(List.of(movie));

        // Act
        List<Movie> movies = directorService.getMoviesByDirectorId(director.getId());

        // Assert
        assertNotNull(movies);
        assertEquals(1, movies.size());
        assertEquals(movie.getTitle(), movies.get(0).getTitle());
    }

    @Test
    @DisplayName("감독 아이디로 시리즈 조회 - 성공")
    void getSeriesByDirectorId() {
        // Arrange
        when(seriesRepository.findByDirectorId(director.getId())).thenReturn(List.of(series));

        // Act
        List<Series> seriesList = directorService.getSeriesByDirectorId(director.getId());

        // Assert
        assertNotNull(seriesList);
        assertEquals(1, seriesList.size());
        assertEquals(series.getTitle(), seriesList.get(0).getTitle());
    }
}