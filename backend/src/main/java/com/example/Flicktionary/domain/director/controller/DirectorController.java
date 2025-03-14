package com.example.Flicktionary.domain.director.controller;

import com.example.Flicktionary.domain.actor.controller.ActorController;
import com.example.Flicktionary.domain.director.dto.DirectorDto;
import com.example.Flicktionary.domain.director.entity.Director;
import com.example.Flicktionary.domain.director.service.DirectorService;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.global.dto.PageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public ResponseEntity<PageDto<DirectorDto>> getDirectors(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Page<Director> directorPage = directorService.getDirectors(keyword, page, pageSize);
        return ResponseEntity.ok(new PageDto<>(directorPage.map(DirectorDto::new)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDirector(@PathVariable Long id) {
        Optional<Director> directorOpt = directorService.getDirector(id);

        if (directorOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Director director = directorOpt.get();
        List<Movie> movies = directorService.getMoviesByDirectorId(id);
        List<Series> series = directorService.getSeriesByDirectorId(id);

        return ResponseEntity.ok(new DirectorController.DirectorResponse(director, movies, series));
    }

    private record DirectorResponse(Long id, String name, String profilePath, List<ActorController.MovieDTO> movies,
                                    List<ActorController.SeriesDTO> series) {
        public DirectorResponse(Director director, List<Movie> movies, List<Series> series) {
            this(director.getId(), director.getName(), director.getProfilePath(),
                    movies.stream().map(ActorController.MovieDTO::new).toList(),
                    series.stream().map(ActorController.SeriesDTO::new).toList());
        }
    }
}
