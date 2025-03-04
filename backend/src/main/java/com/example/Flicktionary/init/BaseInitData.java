package com.example.Flicktionary.init;

import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.movie.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
