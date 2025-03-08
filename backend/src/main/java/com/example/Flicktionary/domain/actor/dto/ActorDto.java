package com.example.Flicktionary.domain.actor.dto;

import com.example.Flicktionary.domain.actor.entity.Actor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;

@Getter
@AllArgsConstructor
public class ActorDto {
    @NonNull
    private final long id;
    @NonNull
    private final String name;

    public ActorDto(Actor actor) {
        this(actor.getId(), actor.getName());
    }
}
