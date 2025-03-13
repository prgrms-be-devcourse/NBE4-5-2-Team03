package com.example.Flicktionary.domain.favorite.service;

import com.example.Flicktionary.domain.favorite.dto.FavoriteContentDto;
import com.example.Flicktionary.domain.favorite.dto.FavoriteDto;
import com.example.Flicktionary.domain.favorite.entity.Favorite;
import com.example.Flicktionary.domain.favorite.repository.FavoriteRepository;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.series.repository.SeriesRepository;
import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.repository.UserAccountRepository;
import com.example.Flicktionary.global.dto.PageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final UserAccountRepository userAccountRepository;
    private final MovieRepository movieRepository;
    private final SeriesRepository seriesRepository;

    @Transactional
    public FavoriteDto createFavorite(FavoriteDto favoriteDto) {
        UserAccount user = userAccountRepository.findById(favoriteDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 즐겨찾기 목록에 존재하는지 확인
        boolean exists = favoriteRepository.existsByUserAccountIdAndContentTypeAndContentId(
                favoriteDto.getUserId(),
                favoriteDto.getContentType(),
                favoriteDto.getContentId()
        );

        if (exists) {
            throw new IllegalArgumentException("이미 즐겨찾기에 추가된 항목입니다.");
        }

        // contentType에 따라 contentId 검증
        boolean contentExists = false;

        if ("MOVIE".equalsIgnoreCase(String.valueOf(favoriteDto.getContentType()))) {
            contentExists = movieRepository.existsById(favoriteDto.getContentId());
        } else if ("SERIES".equalsIgnoreCase(String.valueOf(favoriteDto.getContentType()))) {
            contentExists = seriesRepository.existsById(favoriteDto.getContentId());
        }

        if (!contentExists) {
            throw new IllegalArgumentException(favoriteDto.getContentId() + "번 ContentID를 찾을 수 없습니다.");
        }

        Favorite favorite = favoriteDto.toEntity(user);

        favoriteRepository.save(favorite);
        return FavoriteDto.fromEntity(favorite);
    }

    public PageDto<FavoriteDto> getUserFavorites(Long userId, int page, int pageSize, String sortBy, String direction) {
        if (!userAccountRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        Sort sort = switch (sortBy) {
            case "rating" ->
                    Sort.by(Sort.Direction.fromString(direction), "movie.averageRating", "series.averageRating");
            case "reviews" -> Sort.by(Sort.Direction.fromString(direction), "movie.ratingCount", "series.ratingCount");
            default -> Sort.by(Sort.Direction.fromString(direction), "id");
        };

        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        Page<Favorite> favorites = favoriteRepository.findAllByUserAccountIdWithContent(userId, pageable);

        // DTO 변환 후 수동 정렬 수행 (ASC/DESC 지원)
        Comparator<FavoriteDto> comparator = Comparator.comparing(f -> getSortValue(f, sortBy));

        if (direction.equalsIgnoreCase("DESC")) {
            comparator = comparator.reversed();
        }

        List<FavoriteDto> sortedFavorites = favorites.stream()
                .map(FavoriteDto::fromEntity)
                .sorted(comparator)
                .toList();

        return new PageDto<>(new PageImpl<>(sortedFavorites, pageable, favorites.getTotalElements()));
    }

    // 정렬 기준 값을 가져오는 메서드
    private double getSortValue(FavoriteDto favoriteDto, String sortBy) {
        if (favoriteDto.getData() instanceof FavoriteContentDto contentDto) {
            return switch (sortBy) {
                case "rating" -> contentDto.getAverageRating();
                case "reviews" -> contentDto.getRatingCount();
                default -> favoriteDto.getId(); // 기본값은 ID 정렬
            };
        }
        return 0.0; // 기본값
    }

    @Transactional
    public void deleteFavorite(Long id) {
        Favorite favorite = favoriteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Favorite not found"));

        favoriteRepository.delete(favorite);
    }
}
