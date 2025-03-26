package com.example.Flicktionary.domain.post.dto;

import lombok.Builder;
import lombok.Data;

@Data // @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor를 포함하는 어노테이션
@Builder
public class PostUpdateRequestDto {
    private Long id;
    private String title;
    private String content;
    private Boolean isSpoiler;
}
