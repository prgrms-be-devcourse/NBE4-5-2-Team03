package com.example.Flicktionary.domain.director.entity;

import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.series.entity.Series;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "director", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<Movie> movies = new ArrayList<>();

    @OneToMany(mappedBy = "director", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<Series> series = new ArrayList<>();

    public Director(Long id, String name, String profilePath) {
        this.id = id;
        this.name = name;
        this.profilePath = profilePath;
    }
}
