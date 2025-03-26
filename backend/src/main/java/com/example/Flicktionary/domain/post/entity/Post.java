package com.example.Flicktionary.domain.post.entity;

import com.example.Flicktionary.domain.user.entity.UserAccount;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "post")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Post {

    // 게시글 id (기본키)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 id (외래키)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount userAccount;

    // 게시글 제목
    @Column(length = 50, nullable = false)
    private String title;

    // 게시글 내용
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 게시글 생성 날짜
    @CreatedDate
    private LocalDateTime createdAt;

    // 게시글 수정 날짜
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 게시글 스포일러 여부 토글
    @Column(columnDefinition = "boolean default false")
    private Boolean isSpoiler;
}
