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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

//    @BeforeEach
//    void setUp() {
//        favoriteDto = new FavoriteDto();
//        favoriteDto.setId(1L);
//        favoriteDto.setUserId(1L);
//        favoriteDto.setContentType(ContentType.MOVIE);
//        favoriteDto.setContentId(1L);
//    }

    @Test
    @DisplayName("Create Favorite")
    void createFavorite() throws Exception{

        Long userId = 1L;
        ContentType contentType = ContentType.MOVIE;
        Long contentId = 999L;

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
                .andDo(print())
                .andExpect(status().isCreated());

    }

    @Test
    @DisplayName("Read Favorite")
    void getFavorite() throws Exception{

        Long userId = 1L;

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
    @DisplayName("Delete Favorite")
    void deleteFavorite() throws Exception{

        Long id = 1L;

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/favorite/%d".formatted(id))
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("중복 저장 테스트")
    void checkDuplicate() throws Exception {
        Long userId = 1L;
        ContentType contentType = ContentType.MOVIE;
        Long contentId = 200L;

        // 첫 번째 저장 (201 Created)
        String content = """
                {
                    "userId": "%d",
                    "contentType": "%s",
                    "contentId": "%d"
                }
                """
                .formatted(userId, contentType, contentId)
                .stripIndent();


        mvc.perform(
                        post("/api/favorite")
                                .content(content)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isCreated());

        // 중복 저장 시도 (IllegalArgumentException 발생, 400 Bad Request)
        mvc.perform(
                        post("/api/favorite")
                                .content(content)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 ContentID 저장 테스트")
    void checkContentId() throws Exception {
        Long userId = 1L;
        ContentType contentType = ContentType.MOVIE;
        Long contentId = 9999L;

        // 존재하지 않는 ContentID 저장 시도 (IllegalArgumentException 발생, 400 Bad Request)
        mvc.perform(
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
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 User 저장 테스트")
    void checkUserId() throws Exception {
        Long userId = 9999L;
        ContentType contentType = ContentType.MOVIE;
        Long contentId = 1L;

        // 존재하지 않는 User 저장 시도 (IllegalArgumentException 발생, 400 Bad Request)
        mvc.perform(
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
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
