package com.example.Flicktionary.domain.director.dto;

import com.example.Flicktionary.domain.director.entity.Director;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DirectorDto {
    private final long id;
    private final String name;

    public DirectorDto(Director director) {
        this(director.getId(), director.getName());
    }
}
