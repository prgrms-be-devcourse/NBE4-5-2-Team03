package com.example.Flicktionary.domain.series.dto;

import com.example.Flicktionary.domain.actor.dto.ActorDto;
import com.example.Flicktionary.domain.series.entity.SeriesCast;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;

@Getter
@AllArgsConstructor
public class SeriesCastDto {

    @NonNull
    private final ActorDto actor;
    private final String characterName;

    public SeriesCastDto(SeriesCast seriesCast) {
        this.actor = new ActorDto(seriesCast.getActor());
        this.characterName = seriesCast.getCharacterName();
    }
}
