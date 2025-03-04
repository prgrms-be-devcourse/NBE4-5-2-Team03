package com.example.Flicktionary.domain.movie.service;

import com.example.Flicktionary.domain.movie.dto.MovieDto;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.tmdb.service.TmdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;
    private final TmdbService tmdbService;

    @Transactional
    public void fetchAndSaveMovies(int pages) { // 이미 저장된 영화의 경우 update
        for (int i = 1; i <= pages; i++) {
            List<MovieDto> movieDtos = tmdbService.fetchMovies(i);

            for (MovieDto movieDto : movieDtos) {
                movieRepository.findByTmdbId(movieDto.id()).ifPresentOrElse(
                        movie -> updateMovie(movie, movieDto),
                        () -> movieRepository.save(movieDto.toEntity())
                );
            }
        }
    }

    private void updateMovie(Movie movie, MovieDto movieDto) {
        movie.setTitle(movieDto.title());
        movie.setOverview(movieDto.overview());
        if (!movieDto.releaseDate().isEmpty()) {
            movie.setReleaseDate(LocalDate.parse(movieDto.releaseDate()));
        }
        movie.setPosterPath(movieDto.posterPath());
    }
}
