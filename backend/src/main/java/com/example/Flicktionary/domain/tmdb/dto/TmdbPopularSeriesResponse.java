package com.example.Flicktionary.domain.tmdb.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class TmdbPopularSeriesResponse {
    private List<TmdbSeriesPopularIdResponse> results;
}
