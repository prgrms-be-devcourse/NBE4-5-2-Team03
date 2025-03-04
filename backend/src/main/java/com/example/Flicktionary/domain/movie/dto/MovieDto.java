package com.example.Flicktionary.domain.movie.dto;

import com.example.Flicktionary.domain.movie.entity.Movie;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record MovieDto(long id, String title, String overview,
                       @JsonProperty("release_date") String releaseDate,
                       @JsonProperty("poster_path") String posterPath) {
    public Movie toEntity() {
        return Movie.builder()
                .tmdbId(this.id)
                .title(this.title)
                .overview(this.overview)
                .releaseDate(releaseDate.isEmpty() ? null : LocalDate.parse(this.releaseDate))
                .posterPath(this.posterPath)
                .build();
    }
}
