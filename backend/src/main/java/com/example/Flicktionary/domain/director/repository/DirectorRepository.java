package com.example.Flicktionary.domain.director.repository;

import com.example.Flicktionary.domain.director.entity.Director;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectorRepository extends JpaRepository<Director, Long> {
}
