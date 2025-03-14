package com.example.Flicktionary.domain.director.repository;

import com.example.Flicktionary.domain.director.entity.Director;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DirectorRepository extends JpaRepository<Director, Long> {
    @Query("SELECT d FROM Director d WHERE LOWER(REPLACE(d.name, ' ', '')) LIKE CONCAT('%', :keyword, '%')")
    Page<Director> findByNameLike(String keyword, Pageable pageable);
}
