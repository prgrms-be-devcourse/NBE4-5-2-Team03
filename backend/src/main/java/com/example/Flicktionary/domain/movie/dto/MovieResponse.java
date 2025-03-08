package com.example.Flicktionary.domain.movie.dto;

import com.example.Flicktionary.domain.movie.entity.Movie;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;

@AllArgsConstructor
@Getter
public class MovieResponse {
    @NonNull
    private final long id;
    @NonNull
    private final long tmdbId;
    @NonNull
    private final String title;
    private final String posterPath;
    @NonNull
    private final float averageRating;
    @NonNull
    private final int ratingCount;

    public MovieResponse(Movie movie) {
        this(movie.getId(), movie.getTmdbId(), movie.getTitle(), movie.getPosterPath(), movie.getAverageRating(), movie.getRatingCount());
    }
}
