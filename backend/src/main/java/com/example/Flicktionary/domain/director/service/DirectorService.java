package com.example.Flicktionary.domain.director.service;

import com.example.Flicktionary.domain.director.entity.Director;
import com.example.Flicktionary.domain.director.repository.DirectorRepository;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Optional<Director> getDirector(Long id) {
        return directorRepository.findById(id);
    }

    public List<Movie> getMoviesByDirectorId(Long id) {
        return movieRepository.findByDirectorId(id);
    }

    public List<Series> getSeriesByDirectorId(Long id) {
        return seriesRepository.findByDirectorId(id);
    }
}
