package com.example.Flicktionary.domain.actor.service;

import com.example.Flicktionary.domain.actor.entity.Actor;
import com.example.Flicktionary.domain.actor.repository.ActorRepository;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.entity.MovieCast;
import com.example.Flicktionary.domain.movie.repository.MovieCastRepository;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.entity.SeriesCast;
import com.example.Flicktionary.domain.series.repository.SeriesCastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActorService {
    private final ActorRepository actorRepository;
    private final MovieCastRepository movieCastRepository;
    private final SeriesCastRepository seriesCastRepository;

    // 특정 배우 조회 (출연 영화 포함)
    public Optional<Actor> getActorById(Long id) {
        return actorRepository.findById(id);
    }

    // 배우가 출연한 영화 리스트 조회
    public List<Movie> getMoviesByActorId(Long actorId) {
        List<MovieCast> movieCasts = movieCastRepository.findMoviesByActorId(actorId);
        return movieCasts.stream()
                .map(MovieCast::getMovie)
                .distinct() // 같은 영화가 중복될 경우 제거
                .sorted(Comparator.comparing(Movie::getReleaseDate).reversed()) // tmdbId 기준으로 정렬, 최신 순 정렬
                .collect(Collectors.toList());
    }

    // 배우가 출연한 시리즈 리스트 조회
    public List<Series> getSeriesByActorId(Long actorId) {
        List<SeriesCast> seriesCasts = seriesCastRepository.findSeriesByActorId(actorId);
        return seriesCasts.stream()
                .map(SeriesCast::getSeries)
                .distinct()
                .sorted(Comparator.comparing(Series::getReleaseStartDate).reversed()) // tmdbId 기준으로 정렬, 최신 순 정렬
                .collect(Collectors.toList());
    }

    // 배우 목록 조회
    public Page<Actor> getActors(String keyword, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        String formattedKeyword = keyword.toLowerCase().replaceAll(" ", "");
        return actorRepository.findByNameLike(keyword, pageable);
    }
}
