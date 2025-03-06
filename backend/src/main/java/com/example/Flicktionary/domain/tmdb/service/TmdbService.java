package com.example.Flicktionary.domain.tmdb.service;

import com.example.Flicktionary.domain.movie.dto.MovieDto;
import com.example.Flicktionary.domain.tmdb.dto.TmdbMovieResponseWithDetail;
import com.example.Flicktionary.domain.tmdb.dto.TmdbMoviesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class TmdbService {
    @Value("${tmdb.access-token}")
    private String accessToken;
    private final RestClient restClient;

    public TmdbService(RestClient.Builder builder) {
        String BASE_URL = "https://api.themoviedb.org/3";
        this.restClient = builder.baseUrl(BASE_URL).build();
    }

    public List<MovieDto> fetchMovies(int page) {
        String url = "/movie/popular?&language=ko-KR&page=" + page;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);

            TmdbMoviesResponse response = restClient.get()
                    .uri(url)
                    .headers(h -> h.addAll(headers))
                    .retrieve()
                    .body(TmdbMoviesResponse.class);

            if (response == null) {
                throw new RuntimeException("TMDB API 응답이 null입니다.");
            }

            return response.getResults();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("TMDB API 요청 실패: " + e.getMessage());
        }
    }

    public TmdbMovieResponseWithDetail fetchMovie(long tmdbId) {
        String url = "/movie/%d?&language=ko-KR&append_to_response=credits".formatted(tmdbId);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);

            return restClient.get()
                    .uri(url)
                    .headers(h -> h.addAll(headers))
                    .retrieve()
                    .body(TmdbMovieResponseWithDetail.class);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("TMDB API 요청 실패: " + e.getMessage());
        }
    }


}
