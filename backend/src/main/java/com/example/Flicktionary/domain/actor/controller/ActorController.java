package com.example.Flicktionary.domain.actor.controller;

import com.example.Flicktionary.domain.actor.dto.ActorDto;
import com.example.Flicktionary.domain.actor.entity.Actor;
import com.example.Flicktionary.domain.actor.service.ActorService;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.global.dto.PageDto;
import com.example.Flicktionary.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/actors")
@RequiredArgsConstructor
public class ActorController {
    private final ActorService actorService;

    @GetMapping("/{actorId}")
    public ResponseEntity<ResponseDto<?>> getActorWithMovies(@PathVariable Long actorId) {
        Optional<Actor> actorOpt = actorService.getActorById(actorId);

        if (actorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseDto.of(HttpStatus.NOT_FOUND.value() + "", "%d번 배우가 없습니다.".formatted(actorId)));
        }

        Actor actor = actorOpt.get();
        List<Movie> movies = actorService.getMoviesByActorId(actorId);
        List<Series> series = actorService.getSeriesByActorId(actorId);

        return ResponseEntity.ok(ResponseDto.ok(new ActorResponse(actor, movies, series)));
    }

    @GetMapping
    public ResponseEntity<ResponseDto<PageDto<ActorDto>>> getActors(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Page<Actor> actorPage = actorService.getActors(keyword, page, pageSize);

        return ResponseEntity.ok(ResponseDto.ok(new PageDto<>(actorPage.map(ActorDto::new))));
    }

    // DTO 클래스 (내부 클래스로 정의 가능)
    private record ActorResponse(Long id, String name, String profilePath, List<MovieDTO> movies,
                                 List<SeriesDTO> series) {
        public ActorResponse(Actor actor, List<Movie> movies, List<Series> series) {
            this(actor.getId(), actor.getName(), actor.getProfilePath(),
                    movies.stream().map(MovieDTO::new).toList(),
                    series.stream().map(SeriesDTO::new).toList());
        }
    }

    public record MovieDTO(Long id, String title, String posterPath, String releaseDate) {
        public MovieDTO(Movie movie) {
            this(movie.getId(), movie.getTitle(), movie.getPosterPath(),
                    movie.getReleaseDate() != null ? movie.getReleaseDate().toString() : null);
        }
    }

    public record SeriesDTO(Long id, String title, String posterPath, String releaseStartDate, String releaseEndDate) {
        public SeriesDTO(Series series) {
            this(series.getId(), series.getTitle(), series.getImageUrl(),
                    series.getReleaseStartDate() != null ? series.getReleaseStartDate().toString() : null,
                    series.getReleaseEndDate() != null ? series.getReleaseEndDate().toString() : null);
        }
    }
}
