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
import com.example.Flicktionary.domain.movie.entity.MovieCast;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.tmdb.dto.TmdbMovieResponseWithDetail;
import com.example.Flicktionary.domain.tmdb.service.TmdbService;
import com.example.Flicktionary.global.dto.PageDto;
import lombok.RequiredArgsConstructor;
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

    private final String BASE_IMAGE_URL = "https://image.tmdb.org/t/p";

    // tmdb api를 이용해서 영화 정보를 받아와 저장합니다.
    // 이미 있는 영화는 정보를 업데이트합니다.
    @Transactional
    public void fetchAndSaveMovies(Object source) { // source: Integer(페이지 수) 또는 String(키워드)
        List<MovieDto> movieDtos;

        if (source instanceof Integer pages) { // 인기 영화를 받아 초기데이터 세팅
            movieDtos = new ArrayList<>();
            for (int i = 1; i <= pages; i++) {
                movieDtos.addAll(tmdbService.fetchMovies(i));
            }
        } else if (source instanceof String keyword) { // 키워드로 검색
            movieDtos = tmdbService.searchMovies(keyword);
        } else {
            throw new IllegalArgumentException("지원되지 않는 검색 타입입니다.");
        }

        for (MovieDto movieDto : movieDtos) {
            movieRepository.findByTmdbId(movieDto.id()).ifPresentOrElse(
                    movie -> updateMovie(movie, movieDto),
                    () -> movieRepository.save(movieDto.toEntity(BASE_IMAGE_URL))
            );
        }
    }

    private void updateMovie(Movie movie, MovieDto movieDto) {
        movie.setTitle(movieDto.title());
        movie.setOverview(movieDto.overview());
        movie.setReleaseDate(movieDto.releaseDate().isEmpty() ? null : LocalDate.parse(movieDto.releaseDate()));
        movie.setPosterPath(movieDto.posterPath() == null ? null : BASE_IMAGE_URL + "/w342" + movieDto.posterPath());
    }

    @Transactional
    public PageDto<MovieResponse> getMovies(String keyword, int page, int pageSize, String sortBy) {
        Sort sort = getSort(sortBy);
        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        String formattedKeyword = keyword.toLowerCase().replaceAll(" ", "");

        Page<Movie> movies = movieRepository.findByTitleLike(formattedKeyword, pageable);

        // 검색 결과가 없으면 tmdb api를 통해 데이터를 가져옵니다.
        if (movies.isEmpty()) {
            fetchAndSaveMovies(keyword);
            movies = movieRepository.findByTitleLike(formattedKeyword, pageable);
        }

        return new PageDto<>(movies.map(MovieResponse::new));
    }

    private Sort getSort(String sortBy) {
        return switch (sortBy) {
            case "id" -> Sort.by(Sort.Direction.ASC, "id");
            case "rating" -> Sort.by(Sort.Direction.DESC, "averageRating");
            case "ratingCount" -> Sort.by(Sort.Direction.DESC, "ratingCount");
            default -> throw new RuntimeException("잘못된 정렬 기준입니다.");
        };
    }

    @Transactional
    public MovieResponseWithDetail getMovie(long id) {
        // fetch join을 이용해서 영화에 연관된 배우와 감독 정보를 가져옵니다.
        // 장르는 lazy loading
        Movie movie = movieRepository.findByIdWithCastsAndDirector(id).orElseThrow(
                () -> new NoSuchElementException("%d번 영화를 찾을 수 없습니다.".formatted(id))
        );

        // 상세 조회를 한 적이 없거나 상태가 개봉 전이고 상세 조회한 지 7일이 지났다면 tmdb api를 이용해서 상세 데이터를 받아옵니다.
        if (movie.getFetchDate() == null ||
                (!movie.getStatus().equals("Released") &&
                        movie.getFetchDate().isBefore(LocalDate.now().minusDays(7)))) {
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
        movie.setPosterPath(response.posterPath() == null ? null : BASE_IMAGE_URL + "/w342" + response.posterPath());
        movie.setRuntime(response.runtime());
        movie.setProductionCountry(response.productionCountries().isEmpty() ? null : response.productionCountries().getFirst().name());
        movie.setProductionCompany(response.productionCompanies().isEmpty() ? null : response.productionCompanies().getFirst().name());
        movie.setFetchDate(LocalDate.now());

        // 장르 저장
        List<Genre> genres = new ArrayList<>(response.genres().stream()
                .map(g -> genreRepository.findByName(g.name())
                        .orElseGet(() -> genreRepository.save(new Genre(g.id(), g.name()))))
                .toList());  // 불변 리스트를 가변 리스트로 변환
        movie.setGenres(genres);

        // 배우 저장
        // 기존 캐스팅 목록을 비운 후 새 목록 추가 (orphanRemoval 문제 해결)
        movie.getCasts().clear(); // 기존 데이터 삭제
        List<MovieCast> newCasts = response.credits().cast().stream()
                .limit(5) // 상위 5명만 저장
                .map(a -> {
                    Actor actor = actorRepository.findById(a.id())
                            .orElseGet(() -> actorRepository.save(new Actor(a.id(), a.name(),
                                    a.profilePath() == null ? null : BASE_IMAGE_URL + "/w185" + a.profilePath())));
                    return MovieCast.builder().movie(movie).actor(actor).characterName(a.character()).build();
                })
                .toList();
        movie.getCasts().addAll(newCasts); // 새로운 데이터 추가

        // 감독 저장
        Optional<TmdbMovieResponseWithDetail.TmdbCrew> directorData = response.credits().crew().stream()
                .filter(c -> "Director".equals(c.job()))
                .findFirst();
        directorData.ifPresent(d -> {
            Director director = directorRepository.findById(d.id())
                    .orElseGet(() ->
                            directorRepository.save(new Director(d.id(), d.name(),
                                    d.profilePath() == null ? null : BASE_IMAGE_URL + "/w185" + d.profilePath()))
                    );

            // 감독이 연출한 영화 리스트에 현재 영화 추가
            if (!director.getMovies().contains(movie)) {  // 중복 추가 방지
                director.getMovies().add(movie);
            }
            movie.setDirector(director);
        });

        return movieRepository.save(movie);
    }

    @Transactional
    public void fetchMovieDetail() {
        List<Movie> movies = movieRepository.findAll();
        for (Movie movie : movies) {
            fetchAndSaveMovie(movie);
        }
    }
}
