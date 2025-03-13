package com.example.Flicktionary.global.init;

import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.movie.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    private final MovieService movieService;
    private final MovieRepository movieRepository;

    @Bean
    public ApplicationRunner applicationRunner() {
        return args -> {
            if (movieRepository.count() > 0) {
                return;
            }

            movieService.fetchAndSaveMovies(50);
            // 영화 데이터 초기 저장하면서 세부 정보 초기화
            // 필요 시 주석 해제하고 사용하세요
            // 초기 로드 시 10분 걸림!!!
//            movieService.fetchMovieDetail();
        };
    }


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
