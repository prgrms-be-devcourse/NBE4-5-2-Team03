package com.example.Flicktionary.domain.tmdb.dto;

import com.example.Flicktionary.domain.movie.dto.MovieDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class TmdbMoviesResponse {
    private final List<MovieDto> results;
}
