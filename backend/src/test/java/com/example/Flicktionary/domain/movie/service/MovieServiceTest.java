package com.example.Flicktionary.domain.movie.service;

import com.example.Flicktionary.domain.movie.entity.Movie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class MovieServiceTest {
    @Autowired
    private MovieService movieService;

    @Test
    @DisplayName("영화 목록 조회 - 성공 - 기본")
    void getMovies1() {
        String keyword = "";
        int page = 1;
        int pageSize = 10;
        String sortBy = "id";

        Page<Movie> result = movieService.getMovies(keyword, page, pageSize, sortBy);

        assertThat(result).isNotNull();
        assertThat(result.getSize()).isEqualTo(pageSize);
        assertThat(result.getNumber()).isEqualTo(page - 1);

        List<Movie> movies = result.getContent();
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

        Page<Movie> result = movieService.getMovies(keyword, page, pageSize, sortBy);

        assertThat(result).isNotNull();
        assertThat(result.getSize()).isEqualTo(pageSize);

        List<Movie> movies = result.getContent();

        // 검색 키워드 포함 검증
        for (Movie movie : movies) {
            assertThat(movie.getTitle()).containsIgnoringCase(keyword);
        }
    }

    @Test
    @DisplayName("영화 목록 조회 - 성공 - 평점순 정렬")
    void getMovie3() {
        String keyword = "";
        int page = 1;
        int pageSize = 10;
        String sortBy = "rating";

        Page<Movie> result = movieService.getMovies(keyword, page, pageSize, sortBy);

        assertThat(result).isNotNull();
        assertThat(result.getSize()).isEqualTo(pageSize);
        assertThat(result.getNumber()).isEqualTo(page - 1);

        List<Movie> movies = result.getContent();
        assertThat(movies.size()).isGreaterThan(0);

        // 평점 내림차순 검증
        for (int i = 1; i < movies.size(); i++) {
            assertThat(movies.get(i - 1).getAverageRating()).isGreaterThanOrEqualTo(movies.get(i).getAverageRating());
        }
    }

    @Test
    @DisplayName("영화 목록 조회 - 실패 - 잘못된 정렬 기준")
    void getMovies4() {
        String keyword = "";
        int page = 1;
        int pageSize = 10;
        String sortBy = "unknown";

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> movieService.getMovies(keyword, page, pageSize, sortBy));
        assertThat(exception.getMessage()).isEqualTo("잘못된 정렬기준입니다.");
    }
}