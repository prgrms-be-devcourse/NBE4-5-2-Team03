package com.example.Flicktionary.domain.post.repository;

import com.example.Flicktionary.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 제목으로 게시글 찾기
    Page<Post> findByTitleContaining(String title, Pageable pageable);

    // 내용으로 게시글 찾기
    Page<Post> findByContentContaining(String content, Pageable pageable);

    // 닉네임으로 게시글 찾기
    Page<Post> findByUserAccount_NicknameContaining(String nickname, Pageable pageable);
}
