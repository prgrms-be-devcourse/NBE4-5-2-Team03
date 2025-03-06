package com.example.Flicktionary.domain.tmdb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TmdbSeriesPopularIdResponse {
    @NonNull
    private Long id;
}


