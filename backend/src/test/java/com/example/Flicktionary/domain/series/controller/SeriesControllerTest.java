package com.example.Flicktionary.domain.series.controller;

import com.example.Flicktionary.domain.series.dto.SeriesDetailResponse;
import com.example.Flicktionary.domain.series.dto.SeriesSummaryResponse;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.service.SeriesService;
import com.example.Flicktionary.global.dto.PageDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//이건 전체 빈 로드
//@SpringBootTest
//@AutoConfigureMockMvc

//컨트롤러만 단위테스트 하기위해 SeriesController빈만 로드
@WebMvcTest(controllers = SeriesController.class)
public class SeriesControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SeriesService seriesService;

    @Test
    @DisplayName("Series 목록 조회")
    void getSeriesTest() throws Exception {
        // 테스트할 파라미터 설정
        String keyword = "";
        int page = 1;
        int pageSize = 2;
        String sortBy = "id";

        //given
        List<Series> mockSeriesList = List.of(
                Series.builder()
                        .id(1L)
                        .title("Series 1")
                        .averageRating(2.1)
                        .ratingCount(150)
                        .build(),
                Series.builder()
                        .id(2L)
                        .title("Series 2")
                        .averageRating(3.6)
                        .ratingCount(100)
                        .build(),
                Series.builder()
                        .id(3L)
                        .title("Series 3")
                        .averageRating(3.0)
                        .ratingCount(50)
                        .build()
        );
        Page<Series> mockSeriesPage = new PageImpl<>(mockSeriesList, PageRequest.of(page - 1, pageSize), mockSeriesList.size());
        PageDto<SeriesSummaryResponse> result = new PageDto<>(mockSeriesPage.map(SeriesSummaryResponse::new));

        // mockSeriesPage를 seriesService.getSeries()에서 반환하도록 설정(when)
        when(seriesService.getSeries(keyword, page, pageSize, sortBy)).thenReturn(mockSeriesPage);
        ResultActions resultActions = mvc.perform(get("/api/series")
                        .param("keyword", keyword)
                        .param("page", String.valueOf(page))
                        .param("page-size", String.valueOf(pageSize))
                        .param("sort-by", sortBy)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // 예상 반환값과 API 요청 반환 값 비교(then)
        resultActions
                .andExpect(status().isOk())  // HTTP 상태 코드가 200 OK인지 확인
                .andExpect(handler().handlerType(SeriesController.class))  // 호출된 핸들러가 SeriesController인지 확인
                .andExpect(handler().methodName("getSeries"))  // 호출된 메서드가 getSeries인지 확인
                .andExpect(jsonPath("$.items").isArray())  // 응답의 items가 배열인지 확인
                .andExpect(jsonPath("$.items[0].id").value(result.getItems().get(0).getId()))  // 첫 번째 아이템의 ID 검증
                .andExpect(jsonPath("$.items[1].id").value(result.getItems().get(1).getId()))  // 두 번째 아이템의 ID 검증
                .andExpect(jsonPath("$.totalPages").value(result.getTotalPages()))  // 전체 페이지 수 검증
                .andExpect(jsonPath("$.totalItems").value(result.getTotalItems()));  // 전체 아이템 수 검증
    }

    @Test
    @DisplayName("Series 상세 조회")
    void getSeriesDetailTest() throws Exception {
        // given
        Long seriesId = 1L;
        SeriesDetailResponse response = SeriesDetailResponse.builder()
                .id(seriesId)
                .title("Test Series")
                .imageUrl("http://test.com/image.jpg")
                .averageRating(4.5)
                .ratingCount(100)
                .episode(10)
                .plot("Test Plot")
                .company("Test Company")
                .nation("Test Nation")
                .releaseStartDate(LocalDate.of(2020, 1, 1))
                .releaseEndDate(LocalDate.of(2021, 1, 1))
                .status("Completed")
                .genres(Collections.emptyList())
                .actors(Collections.emptyList())
                .director(null)
                .build();

        //when
        when(seriesService.getSeriesDetail(seriesId)).thenReturn(response);

        //then
        mvc.perform(get("/api/series/{id}", seriesId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(seriesId))
                .andExpect(jsonPath("$.title").value("Test Series"))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.ratingCount").value(100))
                .andExpect(jsonPath("$.episode").value(10))
                .andExpect(jsonPath("$.plot").value("Test Plot"))
                .andExpect(jsonPath("$.company").value("Test Company"))
                .andExpect(jsonPath("$.nation").value("Test Nation"))
                .andExpect(jsonPath("$.status").value("Completed"))
                .andDo(print());
    }
}
