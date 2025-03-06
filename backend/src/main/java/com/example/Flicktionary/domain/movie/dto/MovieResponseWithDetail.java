package com.example.Flicktionary.domain.movie.dto;

import com.example.Flicktionary.domain.actor.dto.ActorDto;
import com.example.Flicktionary.domain.director.dto.DirectorDto;
import com.example.Flicktionary.domain.genre.dto.GenreDto;
import com.example.Flicktionary.domain.movie.entity.Movie;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Getter
public class MovieResponseWithDetail {
    private final long id;
    private final long tmdbId;
    private final String title;
    private final String overview;
    private final LocalDate releaseDate;
    private final String posterPath;
    private final int runtime;
    private final String productionCountry;
    private final String productionCompany;
    private final float averageRating;
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
                movie.getRuntime(),
                movie.getProductionCountry(),
                movie.getProductionCompany(),
                movie.getAverageRating(),
                movie.getGenres().stream().map(g -> new GenreDto(g.getId(), g.getName())).toList(),
                movie.getActors().stream().map(ActorDto::new).toList(),
                new DirectorDto(movie.getDirector())
        );
    }
}
