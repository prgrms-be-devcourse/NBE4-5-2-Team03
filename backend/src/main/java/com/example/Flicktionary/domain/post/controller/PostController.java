package com.example.Flicktionary.domain.post.controller;

import com.example.Flicktionary.domain.post.dto.PostCreateRequestDto;
import com.example.Flicktionary.domain.post.dto.PostResponseDto;
import com.example.Flicktionary.domain.post.dto.PostUpdateRequestDto;
import com.example.Flicktionary.domain.post.service.PostService;
import com.example.Flicktionary.global.dto.PageDto;
import com.example.Flicktionary.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @PostMapping
    public ResponseEntity<ResponseDto<PostResponseDto>> create(
            @RequestBody PostCreateRequestDto postDto) {
        PostResponseDto post = postService.create(postDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.of(HttpStatus.CREATED.value() + "", HttpStatus.CREATED.getReasonPhrase(), post));
    }

    // 특정 게시글 조회
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<PostResponseDto>> getPostById(
            @PathVariable Long id) {
        PostResponseDto post = postService.findById(id);
        return ResponseEntity.ok(ResponseDto.ok(post));
    }

    // 게시글 목록 조회 및 게시글 검색
    @GetMapping
    public ResponseEntity<ResponseDto<PageDto<PostResponseDto>>> getPostList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String keywordType) {
        PageDto<PostResponseDto> postList = postService.getPostList(page, pageSize, keyword, keywordType);
        return ResponseEntity.ok(ResponseDto.ok(postList));
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<PostResponseDto>> update(
            @PathVariable Long id,
            @RequestBody PostUpdateRequestDto postDto) {
        PostResponseDto post = postService.update(id, postDto);
        return ResponseEntity.ok(ResponseDto.ok(post));
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<?>> delete(
            @PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ResponseDto.of(HttpStatus.NO_CONTENT.value() + "", HttpStatus.NO_CONTENT.getReasonPhrase()));
    }
}
