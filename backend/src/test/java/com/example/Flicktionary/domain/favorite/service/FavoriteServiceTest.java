package com.example.Flicktionary.domain.favorite.service;

import com.example.Flicktionary.domain.favorite.dto.FavoriteDto;
import com.example.Flicktionary.domain.favorite.entity.ContentType;
import com.example.Flicktionary.domain.favorite.entity.Favorite;
import com.example.Flicktionary.domain.favorite.repository.FavoriteRepository;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.repository.SeriesRepository;
import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.entity.UserAccountType;
import com.example.Flicktionary.domain.user.repository.UserAccountRepository;
import com.example.Flicktionary.global.dto.PageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class FavoriteServiceTest {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private SeriesRepository seriesRepository;

    private UserAccount testUser;
    private Movie testMovie1;
    private Movie testMovie2;
    private Series testSeries;

    private Long movieCount;
    private Long seriesCount;

    @BeforeEach
    void setUp() {
        movieCount = movieRepository.count();
        seriesCount = seriesRepository.count();

        // 테스트용 User 저장
        testUser = userAccountRepository.save(UserAccount.builder()
                .username("tester1234")
                .password("tester1234")
                .email("tester1234@email.com")
                .nickname("tester1234")
                .role(UserAccountType.USER)
                .build());

        // 테스트용 Movie 저장
        testMovie1 = movieRepository.save(Movie.builder()
                .tmdbId(9599L)
                .title("test movie1")
                .overview("test movie1")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .posterPath("test movie1")
                .productionCountry("KR")
                .productionCompany("test movie1")
                .status("test movie1")
                .averageRating(4)
                .ratingCount(15)
                .build());

        // 테스트용 Movie2 저장
        testMovie2 = movieRepository.save(Movie.builder()
                .tmdbId(599L)
                .title("test movie2")
                .overview("test movie2")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .posterPath("test movie2")
                .productionCountry("KR")
                .productionCompany("test movie2")
                .status("test movie2")
                .averageRating(3.2)
                .ratingCount(151)
                .build());

        // 테스트용 Series1 저장
        testSeries = seriesRepository.save(Series.builder()
                .tmdbId(9519L)
                .title("test series1")
                .plot("test series1")
                .episode(1)
                .status("test series1")
                .imageUrl("test series1")
                .releaseStartDate(LocalDate.of(2024, 1, 1))
                .releaseEndDate(LocalDate.of(2024, 1, 1))
                .nation("KR")
                .company("test series1")
                .averageRating(4.3)
                .ratingCount(5)
                .build());
    }

    @Test
    @DisplayName("즐겨찾기 추가 성공")
    void createFavorite_Success() {
        FavoriteDto favoriteDto = FavoriteDto.builder()
                .userId(testUser.getId())
                .contentType(ContentType.MOVIE)
                .contentId(testMovie1.getId())
                .build();

        favoriteService.createFavorite(favoriteDto);

        List<Favorite> favorites = favoriteRepository.findAll();
        assertThat(favorites).hasSize(1);
        assertThat(favorites.get(0).getUserAccount().getId()).isEqualTo(testUser.getId());
        assertThat(favorites.get(0).getContentType()).isEqualTo(ContentType.MOVIE);
        assertThat(favorites.get(0).getContentId()).isEqualTo(testMovie1.getId());
    }

    @Test
    @DisplayName("중복된 즐겨찾기 추가 시 예외 발생")
    void createFavorite_Duplicate_ThrowsException() {
        FavoriteDto favoriteDto = FavoriteDto.builder()
                .userId(testUser.getId())
                .contentType(ContentType.MOVIE)
                .contentId(testMovie1.getId())
                .build();

        favoriteService.createFavorite(favoriteDto);

        assertThrows(IllegalArgumentException.class, () -> {
            favoriteService.createFavorite(favoriteDto);
        });
    }

    @Test
    @DisplayName("존재하지 않는 User 추가 시 예외 발생")
    void createFavorite_UserNotFound_ThrowsException() {
        FavoriteDto favoriteDto = FavoriteDto.builder()
                .userId(9999L)
                .contentType(ContentType.MOVIE)
                .contentId(testMovie1.getId())
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            favoriteService.createFavorite(favoriteDto);
        });
    }

    @Test
    @DisplayName("존재하지 않는 Content 추가 시 예외 발생")
    void createFavorite_ContentNotFound_ThrowsException() {
        FavoriteDto favoriteDto = FavoriteDto.builder()
                .userId(testUser.getId())
                .contentType(ContentType.MOVIE)
                .contentId(999999999L)
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            favoriteService.createFavorite(favoriteDto);
        });
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 - ID 정렬 및 페이징")
    void getUserFavorites_SortById() {
        // Given
        FavoriteDto favorite1 = FavoriteDto.builder()
                .userId(testUser.getId())
                .contentType(ContentType.MOVIE)
                .contentId(testMovie1.getId())
                .build();
        FavoriteDto favorite2 = FavoriteDto.builder()
                .userId(testUser.getId())
                .contentType(ContentType.SERIES)
                .contentId(testSeries.getId())  // 가상의 Series ID
                .build();

        favoriteService.createFavorite(favorite1);
        favoriteService.createFavorite(favorite2);

        int page = 1;
        int pageSize = 5;
        String sortBy = "id";
        String direction = "desc";

        // When
        PageDto<FavoriteDto> favorites = favoriteService.getUserFavorites(
                testUser.getId(), page, pageSize, sortBy, direction);

        // Then
        assertThat(favorites).isNotNull();
        assertThat(favorites.getItems()).hasSize(2);
        assertThat(favorites.getCurPageNo()).isEqualTo(page);
        assertThat(favorites.getPageSize()).isEqualTo(pageSize);
        assertThat(favorites.getSortBy()).isEqualTo(sortBy + ": " + direction.toUpperCase());
        assertThat(favorites.getItems().get(0).getContentId()).isEqualTo(testMovie1.getId());
        assertThat(favorites.getItems().get(1).getContentId()).isEqualTo(testSeries.getId());
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 - 평점 정렬")
    void getUserFavorites_SortByRating() {
        // Given
        FavoriteDto favorite1 = FavoriteDto.builder()
                .userId(testUser.getId())
                .contentType(ContentType.MOVIE)
                .contentId(testMovie1.getId())
                .build();
        FavoriteDto favorite2 = FavoriteDto.builder()
                .userId(testUser.getId())
                .contentType(ContentType.SERIES)
                .contentId(testSeries.getId())  // 가상의 Series ID
                .build();

        favoriteService.createFavorite(favorite1);
        favoriteService.createFavorite(favorite2);

        int page = 1;
        int pageSize = 2;
        String sortBy = "rating";
        String direction = "asc";

        // When
        PageDto<FavoriteDto> favorites = favoriteService.getUserFavorites(testUser.getId(), page, pageSize, sortBy, direction);

        // Then
        assertThat(favorites).isNotNull();
        assertThat(favorites.getItems()).hasSize(2);
        assertThat(favorites.getItems().get(0).getContentId()).isEqualTo(testMovie1.getId());
        assertThat(favorites.getItems().get(1).getContentId()).isEqualTo(testMovie2.getId());
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 - 리뷰 개수 정렬")
    void getUserFavorites_SortByReviews() {
        // Given
        FavoriteDto favorite1 = FavoriteDto.builder()
                .userId(testUser.getId())
                .contentType(ContentType.MOVIE)
                .contentId(testMovie1.getId())
                .build();
        FavoriteDto favorite2 = FavoriteDto.builder()
                .userId(testUser.getId())
                .contentType(ContentType.SERIES)
                .contentId(testSeries.getId())  // 가상의 Series ID
                .build();

        favoriteService.createFavorite(favorite1);
        favoriteService.createFavorite(favorite2);

        int page = 1;
        int pageSize = 2;
        String sortBy = "reviews";
        String direction = "desc";

        // When
        PageDto<FavoriteDto> favorites = favoriteService.getUserFavorites(testUser.getId(), page, pageSize, sortBy, direction);

        // Then
        assertThat(favorites).isNotNull();
        assertThat(favorites.getItems()).hasSize(2);
        assertThat(favorites.getItems().get(0).getContentId()).isEqualTo(testMovie1.getId());
        assertThat(favorites.getItems().get(1).getContentId()).isEqualTo(testMovie2.getId());
    }

    @Test
    @DisplayName("잘못된 정렬 기준 입력 시 예외 발생")
    void getUserFavorites_InvalidSortBy_ThrowsException() {
        int page = 1;
        int pageSize = 5;
        String sortBy = "unknown";
        String direction = "descasd";

        assertThrows(IllegalArgumentException.class, () -> favoriteService.getUserFavorites(testUser.getId(), page, pageSize, sortBy, direction));
    }
}