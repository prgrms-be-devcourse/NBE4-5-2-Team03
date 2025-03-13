package com.example.Flicktionary.domain.actor.controller;

import com.example.Flicktionary.domain.actor.entity.Actor;
import com.example.Flicktionary.domain.actor.service.ActorService;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.series.entity.Series;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/actors")
@RequiredArgsConstructor
public class ActorController {
    private final ActorService actorService;

    @GetMapping("/{actorId}")
    public ResponseEntity<?> getActorWithMovies(@PathVariable Long actorId) {
        Optional<Actor> actorOpt = actorService.getActorById(actorId);

        if (actorOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Actor actor = actorOpt.get();
        List<Movie> movies = actorService.getMoviesByActorId(actorId);
        List<Series> series = actorService.getSeriesByActorId(actorId);

        return ResponseEntity.ok(new ActorResponse(actor, movies, series));
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

    private record MovieDTO(Long id, String title, String posterPath, String releaseDate) {
        public MovieDTO(Movie movie) {
            this(movie.getId(), movie.getTitle(), movie.getPosterPath(),
                    movie.getReleaseDate() != null ? movie.getReleaseDate().toString() : null);
        }
    }

    private record SeriesDTO(Long id, String title, String posterPath, String releaseStartDate, String releaseEndDate) {
        public SeriesDTO(Series series) {
            this(series.getId(), series.getTitle(), series.getImageUrl(),
                    series.getReleaseStartDate() != null ? series.getReleaseStartDate().toString() : null,
                    series.getReleaseEndDate() != null ? series.getReleaseEndDate().toString() : null);
        }
    }
}
