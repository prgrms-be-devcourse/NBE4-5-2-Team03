package com.example.Flicktionary.domain.movie.dto;

import com.example.Flicktionary.domain.actor.dto.ActorDto;
import com.example.Flicktionary.domain.movie.entity.MovieCast;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;

@Getter
@AllArgsConstructor
public class MovieCastDto {
    @NonNull
    private final ActorDto actor;
    private final String characterName;

    public MovieCastDto(MovieCast movieCast) {
        this.actor = new ActorDto(movieCast.getActor());
        this.characterName = movieCast.getCharacterName();
    }
}
