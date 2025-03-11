package com.example.Flicktionary.domain.series.service;

import com.example.Flicktionary.domain.actor.entity.Actor;
import com.example.Flicktionary.domain.actor.repository.ActorRepository;
import com.example.Flicktionary.domain.director.entity.Director;
import com.example.Flicktionary.domain.director.repository.DirectorRepository;
import com.example.Flicktionary.domain.genre.entity.Genre;
import com.example.Flicktionary.domain.genre.repository.GenreRepository;
import com.example.Flicktionary.domain.series.dto.SeriesDetailResponse;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.repository.SeriesRepository;
import com.example.Flicktionary.domain.tmdb.dto.TmdbPopularSeriesResponse;
import com.example.Flicktionary.domain.tmdb.dto.TmdbSeriesDetailResponse;
import com.example.Flicktionary.domain.tmdb.dto.TmdbSeriesPopularIdResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SeriesService {

    private final SeriesRepository seriesRepository;

    private final GenreRepository genreRepository;

    private final ActorRepository actorRepository;

    private final DirectorRepository directorRepository;

    private final RestTemplate restTemplate;

    @Value("${tmdb.access-token}")
    private String accessToken;

    @Value("${tmdb.base-image-url}")
    private String baseImageUrl;

    // 인기도 순으로 DB에 저장(페이지당 20개)
    @PostConstruct
    public void fetchPopularSeries() {

        if (seriesRepository.count() > 0) {
            return;
        }

        int startPage = 1; // 시작 페이지
        int endPage = 3;   // 끝 페이지

        // 페이지 번호에 따라 반복
        for (int page = startPage; page <= endPage; page++) {
            String url = String.format("https://api.themoviedb.org/3/tv/popular?language=ko-KR&page=%d", page);
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", accessToken);

                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<TmdbPopularSeriesResponse> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        TmdbPopularSeriesResponse.class
                );

                if (response.getBody() == null) {
                    throw new RuntimeException("TMDB API 응답이 null입니다.");
                }

                for (TmdbSeriesPopularIdResponse dto : response.getBody().getResults()) {
                    fetchAndSaveSeriesDetails(dto.getId());
                }


            } catch (Exception e) {
                throw new RuntimeException("TMDB API 요청 실패1: " + e.getMessage());
            }
        }
    }

    // 각 시리즈의 상세 정보를 가져와서 DB에 저장
    private void fetchAndSaveSeriesDetails(Long seriesId) throws InterruptedException {
        String url = String.format("https://api.themoviedb.org/3/tv/%d?language=ko-KR&append_to_response=credits", seriesId);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<TmdbSeriesDetailResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    TmdbSeriesDetailResponse.class
            );

            if (response.getBody() == null) {
                throw new RuntimeException("TMDB API 응답이 null입니다.");
            }

            //Genre 엔티티 생성
            List<Genre> genres = response.getBody().getGenres().stream()
                    .map(genreDto ->
                            genreRepository.findById(genreDto.id())
                                    .orElseGet(() -> {
                                        // 만약 DB에 없으면 새로 생성하여 저장
                                        Genre newGenre = new Genre(genreDto.id(), genreDto.name());
                                        genreRepository.save(newGenre);
                                        return newGenre;  // 새로 생성된 장르를 반환
                                    })
                    )
                    .collect(Collectors.toList());

            //Actor 엔티티 생성
            List<Actor> actors = response.getBody().getTmdbCredits().cast().stream()
                    .limit(5)
                    .map(actorDto ->
                            actorRepository.findById(actorDto.id())
                                    .orElseGet(() -> {
                                        Actor newActor = new Actor(actorDto.id(), actorDto.name(), actorDto.profilePath());
                                        actorRepository.save(newActor);
                                        return newActor;
                                    })
                    )
                    .collect(Collectors.toList());

            //Director 엔티티 생성
            Director director = response.getBody().getTmdbCredits().crew().stream()
                    .findFirst()
                    .map(directorDto ->
                            directorRepository.findById(directorDto.id())
                                    .orElseGet(() -> {
                                        Director newDirector = new Director(directorDto.id(), directorDto.name(), directorDto.profilePath());
                                        directorRepository.save(newDirector);
                                        return newDirector;
                                    })
                    )
                    .orElse(null); // 감독이 없으면 null 반환

            seriesRepository.findByTmdbId(response.getBody().getTmdbId()).ifPresentOrElse(
                    series -> updateSeries(series, response),
                    () -> seriesRepository.save(TmdbSeriesDetailResponse.toEntity(response, genres, actors, director, baseImageUrl))
            );


        } catch (Exception e) {
            throw new RuntimeException("TMDB API 요청 실패2: " + e.getMessage());
        }
    }

    //이미 DB에 존재한다면 업데이트
    private void updateSeries(Series series, ResponseEntity<TmdbSeriesDetailResponse> response) {
        TmdbSeriesDetailResponse body = response.getBody();
        series.setTitle(body.getName());
        series.setPlot(body.getOverview());
        series.setEpisode(body.getNumberOfEpisodes());
        series.setStatus(body.getStatus());
        series.setImageUrl(baseImageUrl + body.getPosterPath());
        if (body.getFirstAirDate() != null) {
            series.setReleaseStartDate(LocalDate.parse(body.getFirstAirDate()));
        }
        if (body.getLastAirDate() != null) {
            series.setReleaseEndDate(LocalDate.parse(body.getLastAirDate()));
        }
    }

    //Series 목록 조회(페이징, 정렬)
    public Page<Series> getSeries(String keyword, int page, int pageSize, String sortBy) {
        Sort sort;

        if (sortBy.equalsIgnoreCase("id")) { //ID기준 오름차순
            sort = Sort.by("id").ascending();
        } else if (sortBy.equalsIgnoreCase("rating")) { //평점 기준 내림차순
            sort = Sort.by("averageRating").descending();
        } else if (sortBy.equalsIgnoreCase("rating-count")) { //리뷰 개수 내림차순
            sort = Sort.by("ratingCount").descending();
        } else {
            throw new RuntimeException("잘못된 정렬 방식입니다.");
        }

        if (page < 1) {
            throw new RuntimeException("페이지는 1부터 요청 가능합니다.");
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        return seriesRepository.findByTitleLike(keyword, pageable);
    }

    //Series 상세 조회
    public SeriesDetailResponse getSeriesDetail(Long id) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("id에 해당하는 Series가 존재하지 않습니다."));

        return new SeriesDetailResponse(series);
    }
}
