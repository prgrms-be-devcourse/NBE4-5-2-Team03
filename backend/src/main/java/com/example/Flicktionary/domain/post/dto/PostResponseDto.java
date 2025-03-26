package com.example.Flicktionary.domain.post.dto;

import com.example.Flicktionary.domain.post.entity.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PostResponseDto {
    private Long id;
    private Long userAccountId;
    private String nickname;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isSpoiler;

    public static PostResponseDto fromEntity(Post post) {

        // 유저가 탈퇴할 경우 "탈퇴한 회원"으로 처리
        String nickname = (post.getUserAccount() != null) ? post.getUserAccount().getNickname() : "탈퇴한 회원";

        return PostResponseDto.builder()
                .id(post.getId())
                // 유저가 탈퇴할 경우 null처리
                .userAccountId(post.getUserAccount() != null ? post.getUserAccount().getId() : null)
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .isSpoiler(post.getIsSpoiler())
                .nickname(nickname)
                .build();
    }
}
