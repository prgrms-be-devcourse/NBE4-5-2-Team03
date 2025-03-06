package com.example.Flicktionary.domain.movie.dto;

import com.example.Flicktionary.domain.movie.entity.Movie;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MovieResponse {
    private final long id;
    private final long tmdbId;
    private final String title;
    private final String posterPath;
    private final float averageRating;

    public MovieResponse(Movie movie) {
        this(movie.getId(), movie.getTmdbId(), movie.getTitle(), movie.getPosterPath(), movie.getAverageRating());
    }
}
