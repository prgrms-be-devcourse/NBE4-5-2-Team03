package com.example.Flicktionary.domain.review.controller;

import com.example.Flicktionary.domain.review.dto.ReviewDto;
import com.example.Flicktionary.domain.review.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@ActiveProfiles("ReviewControllerTest")
@Transactional
public class ReviewControllerTest {

    @MockitoBean
    private ReviewService reviewService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("리뷰 생성 테스트")
    void createReview() throws Exception {

        // 테스트용 리뷰 변수로 생성
        ReviewDto reviewDto = new ReviewDto(
                123451L, 12341L, "테스트용 유저",
                123431L, null, 5, "테스트용 리뷰");

        // reviewService에서 createReview가 호출되면, reviewDto를 반환하도록 설정
        when(reviewService.createReview(any(ReviewDto.class))).thenReturn(reviewDto);

        // POST /api/reviews 요청을 MockMvc를 사용하여 수행
        mockMvc.perform(post("/api/reviews")
                        // JSON 요청 전송
                        .contentType(MediaType.APPLICATION_JSON)
                        // 객체를 JSON 문자열로 변환해서 본문에 포함시키기
                        .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isOk()) // 응답 코드 확인
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nickname").value("테스트 유저"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.content").value("테스트 리뷰 내용"));
    }
}
