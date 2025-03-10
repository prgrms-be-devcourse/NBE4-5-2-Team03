package com.example.Flicktionary.domain.favorite.controller;

import com.example.Flicktionary.domain.favorite.dto.FavoriteDto;
import com.example.Flicktionary.domain.favorite.service.FavoriteService;
import com.example.Flicktionary.global.dto.PageDto;
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
    public ResponseEntity<FavoriteDto> createFavorite(@RequestBody FavoriteDto favoriteDto) {
        try {
            FavoriteDto createdFavorite = favoriteService.createFavorite(favoriteDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFavorite);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 특정 사용자 ID의 즐겨찾기 목록 조회
    @GetMapping("/{userId}")
    public ResponseEntity<PageDto<FavoriteDto>> getUserFavorites(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        try {
            PageDto<FavoriteDto> favorites = favoriteService.getUserFavorites(userId, page, pageSize, sortBy, direction);
            return ResponseEntity.ok(favorites);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
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
    public ResponseEntity<Void> deleteFavorite(@PathVariable Long id) {
        favoriteService.deleteFavorite(id);
        return ResponseEntity.noContent().build();
    }
}
