package com.example.Flicktionary.domain.movie.dto;

import com.example.Flicktionary.domain.movie.entity.Movie;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record MovieDto(Long id, String title, String overview,
                       @JsonProperty("release_date") String releaseDate,
                       @JsonProperty("poster_path") String posterPath) {
    public Movie toEntity(String baseImageUrl) {
        return Movie.builder()
                .tmdbId(this.id)
                .title(this.title)
                .overview(this.overview)
                .releaseDate(releaseDate.isEmpty() ? null : LocalDate.parse(this.releaseDate))
                .posterPath(baseImageUrl + this.posterPath)
                .build();
    }
}
