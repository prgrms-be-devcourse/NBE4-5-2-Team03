package com.example.Flicktionary.domain.director.service;

import com.example.Flicktionary.domain.director.entity.Director;
import com.example.Flicktionary.domain.director.repository.DirectorRepository;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.repository.SeriesRepository;
import com.example.Flicktionary.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorRepository directorRepository;
    private final MovieRepository movieRepository;
    private final SeriesRepository seriesRepository;

    public Page<Director> getDirectors(String keyword, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        String formattedKeyword = keyword.toLowerCase().replaceAll(" ", "");
        return directorRepository.findByNameLike(formattedKeyword, pageable);
    }

    public Director getDirector(Long id) {
        return directorRepository.findById(id)
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND.value(), "%d번 감독을 찾을 수 없습니다.".formatted(id)));
    }

    public List<Movie> getMoviesByDirectorId(Long id) {
        return movieRepository.findByDirectorId(id);
    }

    public List<Series> getSeriesByDirectorId(Long id) {
        return seriesRepository.findByDirectorId(id);
    }
}
