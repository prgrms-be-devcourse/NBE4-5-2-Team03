package com.example.Flicktionary.domain.director.dto;

import com.example.Flicktionary.domain.director.entity.Director;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;

@AllArgsConstructor
@Getter
public class DirectorDto {
    @NonNull
    private final long id;
    @NonNull
    private final String name;

    public DirectorDto(Director director) {
        this(director.getId(), director.getName());
    }
}
