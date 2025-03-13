package com.example.Flicktionary.domain.series.service;

import com.example.Flicktionary.domain.series.dto.SeriesDetailResponse;
import com.example.Flicktionary.domain.series.dto.SeriesSummaryResponse;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.global.dto.PageDto;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class SeriesServiceTest {
    @Autowired
    SeriesService seriesService;

    @Test
    @DisplayName("Series 목록 조회 - id 오름차순 정렬")
    void getSeriesSortByIdTest() {
        String keyword = "";
        int page = 1;
        int pageSize = 10;
        String sortBy = "id";

        Page<Series> series = seriesService.getSeries(keyword, page, pageSize, sortBy);
        PageDto<SeriesSummaryResponse> result = new PageDto<>(series.map(SeriesSummaryResponse::new));

        // items 리스트의 id가 오름차순으로 정렬되었는지 확인
        List<SeriesSummaryResponse> items = result.getItems();
        for (int i = 1; i < items.size(); i++) {
            assertTrue(items.get(i - 1).getId() <= items.get(i).getId());
        }
    }

    @Test
    @DisplayName("Series 목록 조회 - 평점 내림차순 정렬")
    void getSeriesSortByRatingTest(){
        String keyword = "";
        int page = 1;
        int pageSize = 10;
        String sortBy = "rating";

        Page<Series> series = seriesService.getSeries(keyword, page, pageSize, sortBy);
        PageDto<SeriesSummaryResponse> result = new PageDto<>(series.map(SeriesSummaryResponse::new));

        // items 리스트의 avgRating이 내림차순으로 정렬되었는지 확인
        List<SeriesSummaryResponse> items = result.getItems();
        for (int i = 1; i < items.size(); i++) {
            assertTrue(items.get(i - 1).getAverageRating() >= items.get(i).getAverageRating());
        }
    }

    @Test
    @DisplayName("Series 목록 조회 - 리뷰 개수 내림차순 정렬")
    void getSeriesSortByRatingCountTest(){
        String keyword = "";
        int page = 1;
        int pageSize = 10;
        String sortBy = "ratingCount";

        Page<Series> series = seriesService.getSeries(keyword, page, pageSize, sortBy);
        PageDto<SeriesSummaryResponse> result = new PageDto<>(series.map(SeriesSummaryResponse::new));

        // items 리스트의 ratingCount가 내림차순으로 정렬되었는지 확인
        List<SeriesSummaryResponse> items = result.getItems();
        for (int i = 1; i < items.size(); i++) {
            assertTrue(items.get(i - 1).getRatingCount() >= items.get(i).getRatingCount());
        }
    }

    @Test
    @DisplayName("Series 목록 조회 - 검색")
    void getSeriesForSearchTest(){
        String keyword = "The";
        int page = 1;
        int pageSize = 10;
        String sortBy = "id";

        Page<Series> series = seriesService.getSeries(keyword, page, pageSize, sortBy);
        PageDto<SeriesSummaryResponse> result = new PageDto<>(series.map(SeriesSummaryResponse::new));

        // items 리스트의 title이 keyword를 포함하는지 확인 (대소문자 구분 없이)
        List<SeriesSummaryResponse> items = result.getItems();
        for (SeriesSummaryResponse item : items) {
            assertTrue(item.getTitle().toLowerCase().contains(keyword.toLowerCase()));
        }
    }

    @Test
    @DisplayName("Series 목록 조회 - 잘못된 정렬 방식 예외 처리")
    void getSeriesSortByFailTest() {
        String keyword = "The";
        int page = 1;
        int pageSize = 10;
        String sortBy = "xx";  // 잘못된 정렬 방식

        // 예외가 발생하는지 확인
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            seriesService.getSeries(keyword, page, pageSize, sortBy);
        });
        assertEquals("잘못된 정렬 방식입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("Series 상세 정보 조회 성공 테스트")
    void testGetSeriesDetail_Success() throws InterruptedException {
        // given
        Long seriesId = 1L;

        // when
        SeriesDetailResponse response = seriesService.getSeriesDetail(seriesId);

        // then
        assertNotNull(response);
        assertEquals(seriesId, response.getId());
    }

    @Test
    @DisplayName("Series 상세 정보 조회 실패 테스트 (존재하지 않는 ID)")
    void testGetSeriesDetail_Fail_NotFound() {
        // given
        Long seriesId = 999L;  // 존재하지 않는 ID

        // when & then
        assertThrows(RuntimeException.class, () -> seriesService.getSeriesDetail(seriesId));
    }
}
