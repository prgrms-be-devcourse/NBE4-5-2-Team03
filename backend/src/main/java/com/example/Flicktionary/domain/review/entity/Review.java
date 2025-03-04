package com.example.Flicktionary.domain.review.entity;

import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.series.domain.Series;
import com.example.Flicktionary.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Review {

    // 리뷰 id (기본키)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 id (외래키)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 영화 id (외래키)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    // 드라마 id (외래키)
    @ManyToOne
    @JoinColumn(name = "series_id")
    private Series series;

    // 평점
    @Column(nullable = false)
    private int rating;

    // 리뷰
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
