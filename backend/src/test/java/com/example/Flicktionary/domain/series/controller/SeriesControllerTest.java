package com.example.Flicktionary.domain.series.controller;

import com.example.Flicktionary.domain.series.dto.SeriesSummaryResponse;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.service.SeriesService;
import com.example.Flicktionary.global.dto.PageDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SeriesControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SeriesService seriesService;


    @Test
    @DisplayName("Series 목록 조회")
    void getSeries() throws Exception {
        String keyword = "";
        int page = 1;
        int pageSize = 10;
        String sortBy = "id";

        ResultActions resultActions = mvc.perform(get("/api/series")
                        .param("keyword", keyword)
                        .param("page", "%d".formatted(page))
                        .param("pageSize", "%d".formatted(pageSize))
                        .param("sortBy", sortBy)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        Page<Series> series = seriesService.getSeries(keyword, page, pageSize, sortBy);
        PageDto<SeriesSummaryResponse> result = new PageDto<>(series.map(SeriesSummaryResponse::new));

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(SeriesController.class))
                .andExpect(handler().methodName("getSeries"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].id").value(result.getItems().getFirst().getId()))
                .andExpect(jsonPath("$.items[1].id").value(result.getItems().get(1).getId()))
                .andExpect(jsonPath("$.totalPages").value(result.getTotalPages()))
                .andExpect(jsonPath("$.totalItems").value(result.getTotalItems()));
    }
}
