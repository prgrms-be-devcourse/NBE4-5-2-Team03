package com.example.Flicktionary.domain.movie.dto;

import com.example.Flicktionary.domain.actor.dto.ActorDto;
import com.example.Flicktionary.domain.director.dto.DirectorDto;
import com.example.Flicktionary.domain.genre.dto.GenreDto;
import com.example.Flicktionary.domain.movie.entity.Movie;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Getter
public class MovieResponseWithDetail {
    @NonNull
    private final Long id;
    @NonNull
    private final Long tmdbId;
    @NonNull
    private final String title;
    private final String overview;
    private final LocalDate releaseDate;
    private final String posterPath;
    private final String status;
    private final Integer runtime;
    private final String productionCountry;
    private final String productionCompany;
    @NonNull
    private final float averageRating;
    @NonNull
    private final int ratingCount;
    private final List<GenreDto> genres;
    private final List<ActorDto> actors;
    private final DirectorDto director;

    public MovieResponseWithDetail(Movie movie) {
        this(movie.getId(),
                movie.getTmdbId(),
                movie.getTitle(),
                movie.getOverview(),
                movie.getReleaseDate(),
                movie.getPosterPath(),
                movie.getStatus(),
                movie.getRuntime(),
                movie.getProductionCountry(),
                movie.getProductionCompany(),
                movie.getAverageRating(),
                movie.getRatingCount(),
                movie.getGenres().stream().map(g -> new GenreDto(g.getId(), g.getName())).toList(),
                movie.getActors().stream().map(ActorDto::new).toList(),
                new DirectorDto(movie.getDirector())
        );
    }
}
