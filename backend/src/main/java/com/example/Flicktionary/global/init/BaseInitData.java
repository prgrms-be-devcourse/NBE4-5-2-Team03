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
        };
    }


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
