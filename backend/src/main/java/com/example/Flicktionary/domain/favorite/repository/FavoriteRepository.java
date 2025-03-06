package com.example.Flicktionary.domain.favorite.repository;

import com.example.Flicktionary.domain.favorite.entity.ContentType;
import com.example.Flicktionary.domain.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findAllByUserId(Long userId);

    boolean existsByUserIdAndContentTypeAndContentId(Long userId, ContentType contentType, Long contentId);

//    @Query("SELECT f FROM Favorite f " +
//            "LEFT JOIN FETCH Movie m ON f.contentId = m.id AND f.contentType = 'MOVIE' " +
//            "LEFT JOIN FETCH Series s ON f.contentId = s.id AND f.contentType = 'SERIES' " +
//            "WHERE f.user.id = :userId")
    @Query("SELECT f FROM Favorite f " +
            "LEFT JOIN FETCH f.movie " +
            "LEFT JOIN FETCH f.series " +
            "WHERE f.user.id = :userId")
    List<Favorite> findAllByUserIdWithContent(@Param("userId") Long userId);


//    @Query("SELECT f, m FROM Favorite f " +
//            "LEFT JOIN Movie m ON f.contentId = m.id " +
//            "WHERE f.user.id = :userId AND f.contentType = 'MOVIE'")
    @Query("SELECT f FROM Favorite f " +
            "LEFT JOIN FETCH f.movie " +
            "WHERE f.user.id = :userId AND f.contentType = 'MOVIE'")
    List<Favorite> findAllMoviesByUserId(@Param("userId") Long userId);


//    @Query("SELECT f, s FROM Favorite f " +
//            "LEFT JOIN Series s ON f.contentId = s.id " +
//            "WHERE f.user.id = :userId AND f.contentType = 'SERIES'")
    @Query("SELECT f FROM Favorite f " +
            "LEFT JOIN FETCH f.series " +
            "WHERE f.user.id = :userId AND f.contentType = 'SERIES'")
    List<Favorite> findAllSeriesByUserId(@Param("userId") Long userId);


}
