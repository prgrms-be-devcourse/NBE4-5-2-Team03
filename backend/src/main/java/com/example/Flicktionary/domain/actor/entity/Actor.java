package com.example.Flicktionary.domain.actor.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Actor {
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    private String profilePath;
}
