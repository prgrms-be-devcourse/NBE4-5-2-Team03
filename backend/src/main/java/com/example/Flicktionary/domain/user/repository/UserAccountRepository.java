package com.example.Flicktionary.domain.user.repository;

import com.example.Flicktionary.domain.user.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * 회원 엔티티 리포지토리
 */
@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
    Optional<UserAccount> findByRefreshToken(String refreshToken);

    Object getUserAccountById(Long id);
}
