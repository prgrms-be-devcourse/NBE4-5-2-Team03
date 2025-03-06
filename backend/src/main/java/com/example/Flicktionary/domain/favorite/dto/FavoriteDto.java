package com.example.Flicktionary.domain.favorite.dto;

import com.example.Flicktionary.domain.favorite.entity.ContentType;
import com.example.Flicktionary.domain.favorite.entity.Favorite;
import com.example.Flicktionary.domain.user.entity.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteDto {
    private Long userId;
    private ContentType contentType;
    private Long contentId;
    private Object data;

    // Entity -> DTO
    public static FavoriteDto fromEntity(Favorite favorite) {
        Object contentData = null;
        if (favorite.getContentType() == ContentType.MOVIE && favorite.getMovie() != null) {
//            contentData = Hibernate.unproxy(favorite.getMovie());
            contentData = favorite.getMovie();
        } else if (favorite.getContentType() == ContentType.SERIES && favorite.getSeries() != null) {
//            contentData = Hibernate.unproxy(favorite.getSeries());
            contentData = favorite.getSeries();
        }

        return FavoriteDto.builder()
                .userId(favorite.getUser().getId())
                .contentType(favorite.getContentType())
                .contentId(favorite.getContentId())
                .data(contentData)
                .build();
    }

    // DTO -> Entity
    public Favorite toEntity(User user) {
        return Favorite.builder()
                .user(user)
                .contentType(this.contentType)
                .contentId(this.contentId)
                .build();
    }
}
