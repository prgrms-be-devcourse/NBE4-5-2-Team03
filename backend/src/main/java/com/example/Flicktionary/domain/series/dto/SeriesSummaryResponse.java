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

    private String title;

    private String imageUrl;

    private double averageRating;

    private int ratingCount;

    public SeriesSummaryResponse(Series series) {
        this.id = series.getId();
        this.title = series.getTitle();
        this.imageUrl = series.getImageUrl();
        this.averageRating = series.getAverageRating();
        this.ratingCount = series.getRatingCount();
    }
}
