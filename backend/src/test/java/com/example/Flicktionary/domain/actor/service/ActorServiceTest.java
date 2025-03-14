package com.example.Flicktionary.domain.actor.service;

import com.example.Flicktionary.domain.actor.entity.Actor;
import com.example.Flicktionary.domain.actor.repository.ActorRepository;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.entity.MovieCast;
import com.example.Flicktionary.domain.movie.repository.MovieCastRepository;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.entity.SeriesCast;
import com.example.Flicktionary.domain.series.repository.SeriesCastRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("배우 서비스 테스트")
@ExtendWith(MockitoExtension.class)
class ActorServiceTest {
    @Mock
    private ActorRepository actorRepository;
    @Mock
    private MovieCastRepository movieCastRepository;
    @Mock
    private SeriesCastRepository seriesCastRepository;
    @InjectMocks
    private ActorService actorService;

    private Actor actor1, actor2, actor3;
    private Movie movie;
    private Series series;

    @BeforeEach
    void setUp() {
        actor1 = new Actor(1L, "actor1", "test1.png");
        actor2 = new Actor(2L, "actor2", "test2.png");
        actor3 = new Actor(3L, "actor3", "test3.png");
        movie = Movie.builder()
                .id(1L)
                .tmdbId(1L)
                .title("movie")
                .posterPath("movie.png")
                .releaseDate(LocalDate.of(2022, 1, 1))
                .build();
        series = Series.builder()
                .id(1L)
                .tmdbId(1L)
                .title("series")
                .imageUrl("series.png")
                .releaseStartDate(LocalDate.of(2022, 1, 1))
                .releaseEndDate(LocalDate.of(2023, 1, 1))
                .build();
    }

    @Test
    @DisplayName("특정 배우 조회 - 성공")
    void getActorById1() {
        given(actorRepository.findById(1L)).willReturn(Optional.of(actor1));

        Optional<Actor> result = actorService.getActorById(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(actor1);
        then(actorRepository).should().findById(1L);
    }

    @Test
    @DisplayName("배우가 출연한 영화 리스트 조회 - 성공")
    void getMoviesByActorId1() {
        // Given
        MovieCast movieCast = new MovieCast(1L, movie, actor1, "name");
        given(movieCastRepository.findMoviesByActorId(1L)).willReturn(List.of(movieCast));

        // When
        List<Movie> result = actorService.getMoviesByActorId(1L);

        // Then
        assertEquals(movie.getTitle(), result.getFirst().getTitle());
        assertEquals(movie.getReleaseDate(), result.getFirst().getReleaseDate());
        then(movieCastRepository).should().findMoviesByActorId(1L);
    }

    @Test
    @DisplayName("배우가 출연한 시리즈 리스트 조회 - 성공")
    void getSeriesByActorId1() {
        // Given
        SeriesCast seriesCast = new SeriesCast(1L, series, actor1, "name");
        given(seriesCastRepository.findSeriesByActorId(1L)).willReturn(List.of(seriesCast));

        // When
        List<Series> result = actorService.getSeriesByActorId(1L);

        // Then
        assertEquals(series.getTitle(), result.getFirst().getTitle());
        assertEquals(series.getReleaseEndDate(), result.getFirst().getReleaseEndDate());
        then(seriesCastRepository).should().findSeriesByActorId(1L);
    }

    @Test
    @DisplayName("배우 목록 조회 - 성공 - 기본")
    void getActors1() {
        String keyword = "";
        int page = 1;
        int pageSize = 10;

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        Page<Actor> mockPage = new PageImpl<>(List.of(actor1, actor2, actor3), pageable, 3);

        given(actorRepository.findByNameLike(any(String.class), captor.capture())).willReturn(mockPage);

        Page<Actor> result = actorService.getActors(keyword, page, pageSize);
        Pageable captured = captor.getValue();

        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(3);
        // Pageable 검증
        assertEquals(pageSize, captured.getPageSize());
        assertEquals(page - 1, captured.getPageNumber());

        // 배우 데이터 검증
        assertEquals(actor1.getId(), result.getContent().getFirst().getId());
        assertEquals(actor1.getProfilePath(), result.getContent().getFirst().getProfilePath());

        then(actorRepository).should().findByNameLike(any(String.class), any(Pageable.class));
    }

    @Test
    @DisplayName("배우 목록 조회 - 성공 - 검색")
    void getActors2() {
        String keyword = "a c t o r";
        int page = 1;
        int pageSize = 10;

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        Page<Actor> mockPage = new PageImpl<>(List.of(actor1, actor2, actor3), pageable, 3);

        given(actorRepository.findByNameLike(any(String.class), captor.capture())).willReturn(mockPage);

        Page<Actor> result = actorService.getActors(keyword, page, pageSize);
        Pageable captured = captor.getValue();

        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(3);
        // Pageable 검증
        assertEquals(pageSize, captured.getPageSize());
        assertEquals(page - 1, captured.getPageNumber());

        // 배우 데이터 검증
        assertEquals(actor1.getId(), result.getContent().getFirst().getId());
        assertEquals(actor1.getProfilePath(), result.getContent().getFirst().getProfilePath());

        then(actorRepository).should().findByNameLike(any(String.class), any(Pageable.class));
    }

    @Test
    @DisplayName("배우 목록 조회 - 성공 - 페이징")
    void getActors3() {
        String keyword = "";
        int page = 2;
        int pageSize = 2;

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        Page<Actor> mockPage = new PageImpl<>(List.of(actor3), pageable, 1);

        given(actorRepository.findByNameLike(any(String.class), captor.capture())).willReturn(mockPage);

        Page<Actor> result = actorService.getActors(keyword, page, pageSize);
        Pageable captured = captor.getValue();

        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(1);
        // Pageable 검증
        assertEquals(pageSize, captured.getPageSize());
        assertEquals(page - 1, captured.getPageNumber());

        // 배우 데이터 검증
        assertEquals(actor3.getId(), result.getContent().getFirst().getId());
        assertEquals(actor3.getProfilePath(), result.getContent().getFirst().getProfilePath());

        then(actorRepository).should().findByNameLike(any(String.class), any(Pageable.class));
    }
}