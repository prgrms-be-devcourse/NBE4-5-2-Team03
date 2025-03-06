package com.example.Flicktionary.domain.user.repository;

import com.example.Flicktionary.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
