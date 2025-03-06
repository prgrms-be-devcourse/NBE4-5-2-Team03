package com.example.Flicktionary.domain.actor.dto;

import com.example.Flicktionary.domain.actor.entity.Actor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ActorDto {
    private final long id;
    private final String name;

    public ActorDto(Actor actor) {
        this(actor.getId(), actor.getName());
    }
}
