package com.example.Flicktionary.domain.movie.service;

import com.example.Flicktionary.domain.movie.dto.MovieResponse;
import com.example.Flicktionary.domain.movie.dto.MovieResponseWithDetail;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.global.dto.PageDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MovieServiceTest {
    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieRepository movieRepository;

    @Test
    @DisplayName("영화 목록 조회 - 성공 - 기본")
    void getMovies1() {
        String keyword = "";
        int page = 1;
        int pageSize = 10;
        String sortBy = "id";

        PageDto<MovieResponse> result = movieService.getMovies(keyword, page, pageSize, sortBy);

        assertThat(result).isNotNull();
        assertThat(result.getPageSize()).isEqualTo(pageSize);
        assertThat(result.getCurPageNo()).isEqualTo(page);

        List<MovieResponse> movies = result.getItems();
        assertThat(movies.size()).isGreaterThan(0);

        // id 오름차순 검증
        for (int i = 1; i < movies.size(); i++) {
            assertThat(movies.get(i).getId()).isGreaterThan(movies.get(i - 1).getId());
        }
    }

    @Test
    @DisplayName("영화 목록 조회 - 성공 - 검색")
    void getMovies2() {
        String keyword = "해리";
        int page = 1;
        int pageSize = 10;
        String sortBy = "id";

        PageDto<MovieResponse> result = movieService.getMovies(keyword, page, pageSize, sortBy);

        assertThat(result).isNotNull();
        assertThat(result.getPageSize()).isEqualTo(pageSize);

        List<MovieResponse> movies = result.getItems();

        // 검색 키워드 포함 검증
        for (MovieResponse movie : movies) {
            assertThat(movie.getTitle()).containsIgnoringCase(keyword);
        }
    }

    @Test
    @DisplayName("영화 목록 조회 - 성공 - 평점 순 정렬")
    void getMovies3() {
        String keyword = "";
        int page = 1;
        int pageSize = 10;
        String sortBy = "rating";

        PageDto<MovieResponse> result = movieService.getMovies(keyword, page, pageSize, sortBy);

        assertThat(result).isNotNull();
        assertThat(result.getPageSize()).isEqualTo(pageSize);
        assertThat(result.getCurPageNo()).isEqualTo(page);

        List<MovieResponse> movies = result.getItems();
        assertThat(movies.size()).isGreaterThan(0);

        // 평점 내림차순 검증
        for (int i = 1; i < movies.size(); i++) {
            assertThat(movies.get(i - 1).getAverageRating()).isGreaterThanOrEqualTo(movies.get(i).getAverageRating());
        }
    }

    @Test
    @DisplayName("영화 목록 조회 - 성공 - 리뷰 많은 순 정렬")
    void getMovies4() {
        String keyword = "";
        int page = 1;
        int pageSize = 10;
        String sortBy = "rating";

        PageDto<MovieResponse> result = movieService.getMovies(keyword, page, pageSize, sortBy);

        assertThat(result).isNotNull();
        assertThat(result.getPageSize()).isEqualTo(pageSize);
        assertThat(result.getCurPageNo()).isEqualTo(page);

        List<MovieResponse> movies = result.getItems();
        assertThat(movies.size()).isGreaterThan(0);

        // 리뷰 수 내림차순 검증
        for (int i = 1; i < movies.size(); i++) {
            assertThat(movies.get(i - 1).getRatingCount()).isGreaterThanOrEqualTo(movies.get(i).getRatingCount());
        }
    }

    @Test
    @DisplayName("영화 목록 조회 - 실패 - 잘못된 정렬 기준")
    void getMovies5() {
        String keyword = "";
        int page = 1;
        int pageSize = 10;
        String sortBy = "unknown";

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> movieService.getMovies(keyword, page, pageSize, sortBy));
        assertThat(exception.getMessage()).isEqualTo("잘못된 정렬기준입니다.");
    }

    @Test
    @DisplayName("영화 상세 조회 - 성공 - 기본")
    void getMovie1() {
        long id = 1L;

        MovieResponseWithDetail result = movieService.getMovie(id);
        Movie movie = movieRepository.findByIdWithActorsAndDirector(id).get();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getTmdbId()).isEqualTo(movie.getTmdbId());
        assertThat(result.getTitle()).isEqualTo(movie.getTitle());
        assertThat(result.getActors().getFirst().getId()).isEqualTo(movie.getActors().getFirst().getId());
        assertThat(result.getGenres().getFirst().getName()).isEqualTo(movie.getGenres().getFirst().getName());
        assertThat(movie.getFetchDate()).isAfterOrEqualTo(LocalDate.now().minusDays(7));
    }

    @Test
    @DisplayName("영화 상세 조회 - 성공 - 오래된 데이터는 새로 받아와 저장")
    void getMovie2() {
        long id = 1L;

        Movie movie = movieRepository.findById(id).get();

        movie.setFetchDate(LocalDate.now().minusDays(10));
        movie.setProductionCountry(null);
        movie.setStatus("Planned");

        movieRepository.save(movie);

        MovieResponseWithDetail result = movieService.getMovie(id);

        Movie newMovie = movieRepository.findById(id).get();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(newMovie.getFetchDate()).isAfterOrEqualTo(LocalDate.now().minusDays(7));
        assertThat(newMovie.getProductionCountry()).isEqualTo(result.getProductionCountry());
    }

    @Test
    @DisplayName("영화 상세 조회 - 실패 - 없는 영화 조회")
    void getMovie3() {
        long id = 1000000000000000000L;

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> movieService.getMovie(id));
        assertThat(exception.getMessage()).isEqualTo("%d번 영화를 찾을 수 없습니다.".formatted(id));

    }
}