package com.example.Flicktionary.domain.movie.controller;

import com.example.Flicktionary.domain.movie.dto.MovieResponse;
import com.example.Flicktionary.domain.movie.dto.MovieResponseWithDetail;
import com.example.Flicktionary.domain.movie.service.MovieService;
import com.example.Flicktionary.global.dto.PageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
@Tag(name = "영화 API", description = "영화 API입니다.")
public class MovieController {
    private final MovieService movieService;

    @Operation(summary = "영화 목록 조회", description = "영화 목록을 조회합니다. (페이징, 검색, 정렬 지원)")
    @GetMapping
    public ResponseEntity<PageDto<MovieResponse>> getMovies(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy) {
        try {
            PageDto<MovieResponse> response = movieService.getMovies(keyword, page, pageSize, sortBy);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "영화 상세 조회", description = "영화 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<MovieResponseWithDetail> getMovie(@PathVariable long id) {
        try {
            MovieResponseWithDetail response = movieService.getMovie(id);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
