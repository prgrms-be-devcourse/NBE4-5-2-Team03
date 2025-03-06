package com.example.Flicktionary.domain.movie.service;

import com.example.Flicktionary.domain.actor.entity.Actor;
import com.example.Flicktionary.domain.actor.repository.ActorRepository;
import com.example.Flicktionary.domain.director.entity.Director;
import com.example.Flicktionary.domain.director.repository.DirectorRepository;
import com.example.Flicktionary.domain.genre.entity.Genre;
import com.example.Flicktionary.domain.genre.repository.GenreRepository;
import com.example.Flicktionary.domain.movie.dto.MovieDto;
import com.example.Flicktionary.domain.movie.dto.MovieResponse;
import com.example.Flicktionary.domain.movie.dto.MovieResponseWithDetail;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.tmdb.dto.TmdbMovieResponseWithDetail;
import com.example.Flicktionary.domain.tmdb.service.TmdbService;
import com.example.Flicktionary.global.dto.PageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;
    private final TmdbService tmdbService;
    private final GenreRepository genreRepository;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;

    @Value("${tmdb.base-image-url}")
    private String baseImageUrl;

    @Transactional
    public void fetchAndSaveMovies(int pages) { // 이미 저장된 영화의 경우 update
        for (int i = 1; i <= pages; i++) {
            List<MovieDto> movieDtos = tmdbService.fetchMovies(i);

            for (MovieDto movieDto : movieDtos) {
                movieRepository.findByTmdbId(movieDto.id()).ifPresentOrElse(
                        movie -> updateMovie(movie, movieDto),
                        () -> movieRepository.save(movieDto.toEntity(baseImageUrl))
                );
            }
        }
    }

    private void updateMovie(Movie movie, MovieDto movieDto) {
        movie.setTitle(movieDto.title());
        movie.setOverview(movieDto.overview());
        movie.setReleaseDate(movieDto.releaseDate().isEmpty() ? null : LocalDate.parse(movieDto.releaseDate()));
        movie.setPosterPath(movieDto.posterPath());
    }

    @Transactional(readOnly = true)
    public PageDto<MovieResponse> getMovies(String keyword, int page, int pageSize, String sortBy) {
        Sort sort;

        if (sortBy.equalsIgnoreCase("id")) {
            sort = Sort.by(Sort.Direction.ASC, "id");
        } else if (sortBy.equalsIgnoreCase("rating")) {
            sort = Sort.by(Sort.Direction.DESC, "averageRating");
        } else {
            throw new RuntimeException("잘못된 정렬기준입니다.");
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);

        Page<Movie> movies = movieRepository.findByTitleLike(keyword, pageable);

        return new PageDto<>(movies.map(MovieResponse::new));
    }

    @Transactional
    public MovieResponseWithDetail getMovie(long id) {
        Movie movie = movieRepository.findByIdWithActorsAndGenresAndDirector(id).orElseThrow(
                () -> new NoSuchElementException("영화를 찾을 수 없습니다.")
        );

        // fetchDate가 7일 이상 지났다면 새로 데이터를 가져옴
        if (movie.getFetchDate() == null || movie.getFetchDate().isBefore(LocalDate.now().minusDays(7))) {
            return new MovieResponseWithDetail(fetchAndSaveMovie(movie));
        }

        return new MovieResponseWithDetail(movie);
    }

    @Transactional
    public Movie fetchAndSaveMovie(Movie movie) {
        TmdbMovieResponseWithDetail response = tmdbService.fetchMovie(movie.getTmdbId());

        movie.setTmdbId(response.tmdbId());
        movie.setTitle(response.title());
        movie.setOverview(response.overview());
        movie.setReleaseDate(response.releaseDate().isEmpty() ? null : LocalDate.parse(response.releaseDate()));
        movie.setStatus(response.status());
        movie.setPosterPath(baseImageUrl + response.posterPath());
        movie.setRuntime(response.runtime());
        movie.setProductionCountry(response.productionCountries().isEmpty() ? "Unknown" : response.productionCountries().getFirst().name());
        movie.setProductionCompany(response.productionCompanies().isEmpty() ? "Unknown" : response.productionCompanies().getFirst().name());
        movie.setFetchDate(LocalDate.now());

        // 장르 저장
        List<Genre> genres = new ArrayList<>(response.genres().stream()
                .map(g -> genreRepository.findByName(g.name())
                        .orElseGet(() -> genreRepository.save(new Genre(g.id(), g.name()))))
                .toList());  // 불변 리스트를 가변 리스트로 변환
        movie.setGenres(genres);

        // 배우 저장
        List<Actor> actors = new ArrayList<>(response.credits().cast().stream()
                .limit(5) // 상위 5명만 저장
                .map(a -> actorRepository.findById(a.id())
                        .orElseGet(() -> actorRepository.save(new Actor(a.id(), a.name()))))
                .toList());  // 불변 리스트를 가변 리스트로 변환
        movie.setActors(actors);

        // 감독 저장
        Optional<TmdbMovieResponseWithDetail.TmdbCrew> directorData = response.credits().crew().stream()
                .filter(c -> "Director".equals(c.job()))
                .findFirst();

        directorData.ifPresent(d -> {
            Director director = directorRepository.findById(d.id())
                    .orElseGet(() -> directorRepository.save(new Director(d.id(), d.name())));
            movie.setDirector(director);
        });

        return movieRepository.save(movie);
    }
}
