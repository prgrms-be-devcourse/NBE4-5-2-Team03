package com.example.Flicktionary.domain.series.dto;

import com.example.Flicktionary.domain.series.entity.Series;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class SeriesSummaryResponse {

    private Long id;

    private Long tmdbId;

    private String title;

    private String posterPath;

    private double averageRating;

    private int ratingCount;

    public SeriesSummaryResponse(Series series) {
        this.id = series.getId();
        this.tmdbId = series.getTmdbId();
        this.title = series.getTitle();
        this.posterPath = series.getImageUrl();
        this.averageRating = series.getAverageRating();
        this.ratingCount = series.getRatingCount();
    }
}
