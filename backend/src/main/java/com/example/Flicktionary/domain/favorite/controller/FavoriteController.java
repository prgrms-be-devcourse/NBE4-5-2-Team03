package com.example.Flicktionary.domain.favorite.controller;

import com.example.Flicktionary.domain.favorite.dto.FavoriteDto;
import com.example.Flicktionary.domain.favorite.service.FavoriteService;
import com.example.Flicktionary.global.dto.PageDto;
import com.example.Flicktionary.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    // 즐겨찾기 추가
    @PostMapping
    public ResponseEntity<ResponseDto<?>> createFavorite(@RequestBody FavoriteDto favoriteDto) {
        try {
            FavoriteDto createdFavorite = favoriteService.createFavorite(favoriteDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.of(HttpStatus.CREATED.value() + "", HttpStatus.CREATED.getReasonPhrase(), createdFavorite));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseDto.of(HttpStatus.BAD_REQUEST.value() + "", e.getMessage()));
        }
    }

    // 특정 사용자 ID의 즐겨찾기 목록 조회
    @GetMapping("/{userId}")
    public ResponseEntity<ResponseDto<?>> getUserFavorites(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        try {
            PageDto<FavoriteDto> favorites = favoriteService.getUserFavorites(userId, page, pageSize, sortBy, direction);
            return ResponseEntity.ok(ResponseDto.ok(favorites));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseDto.of(HttpStatus.BAD_REQUEST.value() + "", e.getMessage()));
        }
    }

//    // 즐겨찾기 수정
//    @PutMapping("/{id}")
//    public ResponseEntity<FavoriteDto> updateFavorite(@PathVariable Long id, @RequestBody FavoriteDto favoriteDto) {
//        FavoriteDto updatedFavorite = favoriteService.updateFavorite(id, favoriteDto);
//        return ResponseEntity.ok(updatedFavorite);
//    }


    // 즐겨찾기 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<?>> deleteFavorite(@PathVariable Long id) {
        favoriteService.deleteFavorite(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ResponseDto.of(HttpStatus.NO_CONTENT.value() + "", HttpStatus.NO_CONTENT.getReasonPhrase()));
    }
}
