package com.example.Flicktionary.domain.series.entity;

import com.example.Flicktionary.domain.actor.entity.Actor;
import com.example.Flicktionary.domain.director.entity.Director;
import com.example.Flicktionary.domain.genre.entity.Genre;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Series {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long tmdbId;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String plot;

    private int episode;

    private String status;

    private String imageUrl;

    private LocalDate releaseStartDate;

    private LocalDate releaseEndDate;

    private String nation;

    private String company;

    @Builder.Default
    private double averageRating = 0.0;

    @Builder.Default
    private int ratingCount = 0;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "series_genre",
            joinColumns = @JoinColumn(name = "series_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<Genre> genres = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeriesCast> casts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id")
    private Director director;
}
