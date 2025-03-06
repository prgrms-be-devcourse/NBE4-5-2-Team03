package com.example.Flicktionary.domain.genre.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class GenreDto {
    @NonNull
    private Long id;

    private final String name;
}
