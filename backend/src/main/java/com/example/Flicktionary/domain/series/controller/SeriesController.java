package com.example.Flicktionary.domain.series.controller;

import com.example.Flicktionary.domain.series.dto.SeriesDetailResponse;
import com.example.Flicktionary.domain.series.dto.SeriesSummaryResponse;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.service.SeriesService;
import com.example.Flicktionary.global.dto.PageDto;
import com.example.Flicktionary.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/series")
public class SeriesController {

    private final SeriesService seriesService;

    //Series 목록 조회(페이징, 정렬)
    @GetMapping
    public ResponseEntity<ResponseDto<PageDto<SeriesSummaryResponse>>> getSeries(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy) {

        Page<Series> series = seriesService.getSeries(keyword, page, pageSize, sortBy);
        PageDto<SeriesSummaryResponse> pageDto = new PageDto<>(series.map(SeriesSummaryResponse::new));
        return ResponseEntity.ok(ResponseDto.ok(pageDto));
    }

    //Series 상세 목록 조회
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<SeriesDetailResponse>> getSeriesDetail(@PathVariable Long id) {
        SeriesDetailResponse seriesDto = seriesService.getSeriesDetail(id);
        return ResponseEntity.ok(ResponseDto.ok(seriesDto));
    }
}
