package com.example.Flicktionary.domain.favorite.controller;

import com.example.Flicktionary.domain.favorite.dto.FavoriteDto;
import com.example.Flicktionary.domain.favorite.entity.ContentType;
import com.example.Flicktionary.domain.favorite.service.FavoriteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
//@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class FavoriteControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private FavoriteService favoriteService;

    private FavoriteDto favoriteDto;

    @Test
    @DisplayName("즐겨찾기 추가 - 성공")
    void createFavorite() throws Exception {
        // Given
        Long userId = 1L;
        ContentType contentType = ContentType.MOVIE;
        Long contentId = 599L;

        // When & Then
        ResultActions resultActions = mvc
                .perform(
                        post("/api/favorite")
                                .content("""
                                        {
                                            "userId": "%d",
                                            "contentType": "%s",
                                            "contentId": "%d"
                                        }
                                        """
                                        .formatted(userId, contentType, contentId)
                                        .stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        resultActions
                .andExpect(status().isCreated())
                .andExpect(handler().handlerType(FavoriteController.class))
                .andExpect(handler().methodName("createFavorite"));

    }

    @Test
    @DisplayName("즐겨찾기 조회 - 성공")
    void getFavorite() throws Exception {
        // Given
        Long userId = 1L;

        // When & Then
        ResultActions resultActions = mvc
                .perform(
                        get("/api/favorite/%d".formatted(userId))
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("즐겨찾기 삭제 - 성공")
    void deleteFavorite() throws Exception {
        // Given
        Long id = 1L;

        // When & Then
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/favorite/%d".formatted(id))
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        resultActions
                .andExpect(status().isNoContent())
                .andExpect(handler().handlerType(FavoriteController.class))
                .andExpect(handler().methodName("deleteFavorite"));
    }

    @Test
    @DisplayName("즐겨찾기에 중복 추가 시 예외 발생")
    void checkDuplicate() throws Exception {
        // Given
        Long userId = 1L;
        ContentType contentType = ContentType.MOVIE;
        Long contentId = 599L;

        String content = """
                {
                    "userId": "%d",
                    "contentType": "%s",
                    "contentId": "%d"
                }
                """
                .formatted(userId, contentType, contentId)
                .stripIndent();

        // When & Then

        // 첫 번째 저장 (201 Created)
        ResultActions resultActions1 = mvc.perform(post("/api/favorite")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions1
                .andExpect(status().isCreated())
                .andExpect(handler().handlerType(FavoriteController.class))
                .andExpect(handler().methodName("createFavorite"));


        // 중복 저장 시도 (IllegalArgumentException 발생, 400 Bad Request)
        ResultActions resultActions2 = mvc.perform(post("/api/favorite")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions2
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(FavoriteController.class))
                .andExpect(handler().methodName("createFavorite"));
    }

    @Test
    @DisplayName("존재하지 않는 ContentID 추가 시 예외 발생")
    void checkContentId() throws Exception {
        // Given
        Long userId = 1L;
        ContentType contentType = ContentType.MOVIE;
        Long contentId = 999999999L;

        // When & Then
        // 존재하지 않는 ContentID 저장 시도 (IllegalArgumentException 발생, 400 Bad Request)
        ResultActions resultActions = mvc.perform(post("/api/favorite")
                        .content("""
                                {
                                    "userId": "%d",
                                    "contentType": "%s",
                                    "contentId": "%d"
                                }
                                """
                                .formatted(userId, contentType, contentId)
                                .stripIndent())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(FavoriteController.class))
                .andExpect(handler().methodName("createFavorite"));
    }

    @Test
    @DisplayName("존재하지 않는 UserId로 즐겨찾기 추가 시 예외 발생")
    void checkUserId() throws Exception {
        // Given
        Long userId = 999999999L;
        ContentType contentType = ContentType.MOVIE;
        Long contentId = 1L;

        // 존재하지 않는 User 저장 시도 (IllegalArgumentException 발생, 400 Bad Request)
        // When & Then
        ResultActions resultActions = mvc.perform(post("/api/favorite")
                        .content("""
                                {
                                    "userId": "%d",
                                    "contentType": "%s",
                                    "contentId": "%d"
                                }
                                """
                                .formatted(userId, contentType, contentId)
                                .stripIndent())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(FavoriteController.class))
                .andExpect(handler().methodName("createFavorite"));
    }

    @Test
    @DisplayName("Read Favorite - Page, id 내림차순 정렬(기본)")
    void getFavoritePage() throws Exception {

        Long userId = 1L;
        int page = 1;
        int pageSize = 5;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/favorite/%d?page=%d&pageSize=%d".formatted(userId, page, pageSize))
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.curPageNo").value(page))
                .andExpect(jsonPath("$.data.pageSize").value(pageSize))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @DisplayName("Read Favorite - Page, id 오름차순 정렬")
    void getFavoritePageSortByIdDesc() throws Exception {

        Long userId = 1L;
        int page = 1;
        int pageSize = 5;
        String sortBy = "id";
        String direction = "asc";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/favorite/{userId}", userId)
                                .param("page", "%d".formatted(page))
                                .param("pageSize", "%d".formatted(pageSize))
                                .param("sortBy", sortBy)
                                .param("direction", direction)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.curPageNo").value(page))
                .andExpect(jsonPath("$.data.pageSize").value(pageSize))
                .andExpect(jsonPath("$.data.sortBy").exists())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @DisplayName("Read Favorite - Page, rating 내림차순 정렬")
    void getFavoritePageSortByRatingDesc() throws Exception {

        Long userId = 1L;
        int page = 1;
        int pageSize = 5;
        String sortBy = "rating";
        String direction = "desc";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/favorite/{userId}", userId)
                                .param("page", "%d".formatted(page))
                                .param("pageSize", "%d".formatted(pageSize))
                                .param("sortBy", sortBy)
                                .param("direction", direction)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.curPageNo").value(page))
                .andExpect(jsonPath("$.data.pageSize").value(pageSize))
                .andExpect(jsonPath("$.data.sortBy").exists())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @DisplayName("Read Favorite - Page, rating 오름차순 정렬")
    void getFavoritePageSortByRatingAsc() throws Exception {

        Long userId = 1L;
        int page = 1;
        int pageSize = 5;
        String sortBy = "rating";
        String direction = "asc";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/favorite/{userId}", userId)
                                .param("page", "%d".formatted(page))
                                .param("pageSize", "%d".formatted(pageSize))
                                .param("sortBy", sortBy)
                                .param("direction", direction)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.curPageNo").value(page))
                .andExpect(jsonPath("$.data.pageSize").value(pageSize))
                .andExpect(jsonPath("$.data.sortBy").exists())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @DisplayName("Read Favorite - Page, reviews 내림차순 정렬")
    void getFavoritePageSortByReviewsDesc() throws Exception {

        Long userId = 1L;
        int page = 1;
        int pageSize = 5;
        String sortBy = "reviews";
        String direction = "desc";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/favorite/{userId}", userId)
                                .param("page", "%d".formatted(page))
                                .param("pageSize", "%d".formatted(pageSize))
                                .param("sortBy", sortBy)
                                .param("direction", direction)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.curPageNo").value(page))
                .andExpect(jsonPath("$.data.pageSize").value(pageSize))
                .andExpect(jsonPath("$.data.sortBy").exists())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @DisplayName("Read Favorite - Page, reviews 오름차순 정렬")
    void getFavoritePageSortByReviewsAsc() throws Exception {

        Long userId = 1L;
        int page = 1;
        int pageSize = 5;
        String sortBy = "reviews";
        String direction = "asc";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/favorite/{userId}", userId)
                                .param("page", "%d".formatted(page))
                                .param("pageSize", "%d".formatted(pageSize))
                                .param("sortBy", sortBy)
                                .param("direction", direction)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.curPageNo").value(page))
                .andExpect(jsonPath("$.data.pageSize").value(pageSize))
                .andExpect(jsonPath("$.data.sortBy").exists())
                .andExpect(jsonPath("$.data.items").isArray());
    }
}
