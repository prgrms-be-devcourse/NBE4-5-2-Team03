package com.example.Flicktionary.domain.review.dto;

import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.review.entity.Review;
import com.example.Flicktionary.domain.series.domain.Series;
import com.example.Flicktionary.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {

    private Long id;
    private Long userId;
    private String nickname;
    private Long movieId;
    private Long seriesId;
    private int rating;
    private String content;

    // Entity를 DTO로 변환
    public static ReviewDto fromEntity(Review review) {

        return ReviewDto.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .nickname(review.getUser().getNickname())
                .movieId(review.getMovie() != null ? review.getMovie().getId() : null)
                .seriesId(review.getSeries() != null ? review.getSeries().getId() : null)
                .rating(review.getRating())
                .content(review.getContent())
                .build();
    }

    // DTO를 Entity로 변환
    public Review toEntity(User user, Movie movie, Series series) {

        return Review.builder()
                .user(user)
                .movie(movie)
                .series(series)
                .rating(this.rating)
                .content(this.content)
                .build();
    }
}
