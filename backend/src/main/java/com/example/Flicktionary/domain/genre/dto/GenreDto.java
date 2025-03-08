package com.example.Flicktionary.domain.genre.dto;

import com.example.Flicktionary.domain.genre.entity.Genre;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;

@Getter
@AllArgsConstructor
public class GenreDto {
    @NonNull
    private final Long id;

    @NonNull
    private final String name;

    public GenreDto(Genre genre) {
        this.id = genre.getId();
        this.name = genre.getName();
    }
}
