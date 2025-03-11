package com.example.Flicktionary.domain.director.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Director {
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    private String profilePath;
}
