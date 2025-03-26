package com.example.Flicktionary.domain.post.dto;

import com.example.Flicktionary.domain.post.entity.Post;
import com.example.Flicktionary.domain.user.entity.UserAccount;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostCreateRequestDto {
    private Long userAccountId;
    private String title;
    private String content;
    private Boolean isSpoiler;

    public Post toEntity(UserAccount userAccount) {
        return Post.builder()
                .userAccount(userAccount)
                .title(this.title)
                .content(this.content)
                .isSpoiler(this.isSpoiler)
                .build();
    }
}
