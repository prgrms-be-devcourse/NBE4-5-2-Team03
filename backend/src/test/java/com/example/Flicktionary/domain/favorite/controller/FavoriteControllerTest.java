package com.example.Flicktionary.domain.favorite.controller;

import com.example.Flicktionary.domain.favorite.dto.FavoriteDto;
import com.example.Flicktionary.domain.favorite.entity.ContentType;
import com.example.Flicktionary.domain.favorite.service.FavoriteService;
import com.example.Flicktionary.domain.movie.controller.MovieController;
import com.example.Flicktionary.domain.movie.service.MovieService;
import com.example.Flicktionary.domain.user.service.UserAccountJwtAuthenticationService;
import com.example.Flicktionary.domain.user.service.UserAccountService;
import com.example.Flicktionary.global.dto.PageDto;
import com.example.Flicktionary.global.security.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("즐겨찾기 도메인 컨트롤러 테스트")
@Import({FavoriteService.class,
        UserAccountService.class,
        UserAccountJwtAuthenticationService.class,
        CustomUserDetailsService.class})
@WebMvcTest(FavoriteController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FavoriteControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FavoriteService favoriteService;

    @MockitoBean
    private UserAccountService userAccountService;

    @MockitoBean
    private UserAccountJwtAuthenticationService userAccountJwtAuthenticationService;

    private FavoriteDto favoriteDto;

    @Test
    @DisplayName("즐겨찾기 추가 - 성공")
    void createFavorite() throws Exception {
        // Given
        Long userId = 1L;
        ContentType contentType = ContentType.MOVIE;
        Long contentId = 599L;
        ArgumentCaptor<FavoriteDto> captor = ArgumentCaptor.forClass(FavoriteDto.class);
        given(favoriteService.createFavorite(captor.capture())).willReturn(FavoriteDto.builder()
                .userId(userId)
                .contentType(contentType)
                .contentId(contentId)
                .build());

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
        FavoriteDto captured = captor.getValue();

        resultActions
                .andExpect(status().isCreated())
                .andExpect(handler().handlerType(FavoriteController.class))
                .andExpect(handler().methodName("createFavorite"))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.contentType").value(contentType.toString()))
                .andExpect(jsonPath("$.data.contentId").value(contentId));
        assertEquals(userId, captured.getUserId());
        assertEquals(contentType, captured.getContentType());
        assertEquals(contentId, captured.getContentId());
        then(favoriteService).should().createFavorite(any(FavoriteDto.class));
    }

    @Test
    @DisplayName("즐겨찾기 조회 - 성공")
    void getFavorite() throws Exception {
        // Given
        Long userId = 1L;
        ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> integerCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        given(favoriteService.getUserFavorites(
                longCaptor.capture(),
                integerCaptor.capture(),
                integerCaptor.capture(),
                stringCaptor.capture(),
                stringCaptor.capture()
        )).willReturn(new PageDto<>(new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                10)));

        // When & Then
        ResultActions resultActions = mvc
                .perform(
                        get("/api/favorite/%d".formatted(userId))
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());
        Long capturedLong = longCaptor.getValue();
        List<Integer> capturedIntegers = integerCaptor.getAllValues();
        List<String> capturedStrings = stringCaptor.getAllValues();

        resultActions
                .andExpect(status().isOk());
        assertEquals(userId, capturedLong);
        assertEquals(1, capturedIntegers.getFirst());
        assertEquals(10, capturedIntegers.get(1));
        assertEquals("id", capturedStrings.getFirst());
        assertEquals("desc", capturedStrings.get(1));
        then(favoriteService).should().getUserFavorites(userId, 1, 10, "id", "desc");
    }

    @Test
    @DisplayName("즐겨찾기 삭제 - 성공")
    void deleteFavorite() throws Exception {
        // Given
        Long id = 1L;
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        doNothing().when(favoriteService).deleteFavorite(captor.capture());

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
        then(favoriteService).should().deleteFavorite(id);
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
        given(favoriteService.createFavorite(any(FavoriteDto.class)))
                .willThrow(new IllegalArgumentException("이미 즐겨찾기에 추가된 항목입니다."));

        // When & Then

        ResultActions resultActions = mvc.perform(post("/api/favorite")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(FavoriteController.class))
                .andExpect(handler().methodName("createFavorite"))
                .andExpect(jsonPath("$.message").value("이미 즐겨찾기에 추가된 항목입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
        then(favoriteService).should().createFavorite(any(FavoriteDto.class));
    }

    @Test
    @DisplayName("존재하지 않는 ContentID 추가 시 예외 발생")
    void checkContentId() throws Exception {
        // Given
        Long userId = 1L;
        ContentType contentType = ContentType.MOVIE;
        Long contentId = 999999999L;
        given(favoriteService.createFavorite(any(FavoriteDto.class)))
                .willThrow(new IllegalArgumentException("%d번 ContentID를 찾을 수 없습니다.".formatted(contentId)));

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
                .andExpect(handler().methodName("createFavorite"))
                .andExpect(jsonPath("$.message").value("%d번 ContentID를 찾을 수 없습니다.".formatted(contentId)));
        then(favoriteService).should().createFavorite(any(FavoriteDto.class));
    }

    @Test
    @DisplayName("존재하지 않는 UserId로 즐겨찾기 추가 시 예외 발생")
    void checkUserId() throws Exception {
        // Given
        Long userId = 999999999L;
        ContentType contentType = ContentType.MOVIE;
        Long contentId = 1L;
        given(favoriteService.createFavorite(any(FavoriteDto.class)))
                .willThrow(new IllegalArgumentException("User not found"));

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
                .andExpect(handler().methodName("createFavorite"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}
