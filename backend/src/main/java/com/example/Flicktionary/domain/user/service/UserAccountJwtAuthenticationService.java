package com.example.Flicktionary.domain.user.service;

import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.repository.UserAccountRepository;
import com.example.Flicktionary.global.utils.JwtUtils;
import com.example.Flicktionary.global.utils.UuidUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * JWT를 통한 회원 인증을 지원하기 위한 서비스.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserAccountJwtAuthenticationService {

    private final UserAccountRepository userAccountRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * JWT 서명/검증에 사용될 비밀키
     */
    @Value("${custom.jwt.secret-key}")
    private String secret;

    /**
     * 접큰 토큰의 유효기간(초)
     */
    @Value("${custom.jwt.access-expire-seconds}")
    private Integer accessExpireSeconds;

    /**
     * 리프레시 토큰의 유효기간(일)
     */
    @Value("${custom.jwt.refresh-expire-days}")
    private Integer refreshExpireDays;

    /**
     * 인증 정보를 검증한 뒤, 해당 회원에게 접근 토큰을 발행한다.
     * @param username 회원의 유저 ID
     * @param password 회원의 비밀번호
     * @return 새로 발행된 접근 토큰
     */
    public String createNewAccessTokenForUser(String username, String password) {
        UserAccount userAccount = getUserByUsername(username);
        if (!passwordEncoder.matches("{bcrypt}" + password, userAccount.getPassword())) {
            throw new RuntimeException("비밀번호가 틀립니다.");
        }
        return createNewAccessTokenWithClaims(userAccount.getUsername(), userAccount.getNickname());
    }

    /**
     * 주어진 유저 ID와 닉네임이 클레임에 담긴 접근 토큰을 발행한다.
     * @param username 회원의 유저 ID
     * @param nickname 회원의 닉네임
     * @return 새로 발행된 접근 토큰
     */
    private String createNewAccessTokenWithClaims(String username, String nickname) {
        Map<String, Object> claims = Map.of(
                "username", username,
                "nickname", nickname
        );
        return JwtUtils.createToken(secret, accessExpireSeconds, claims);
    };

    /**
     * 주어진 리프레시 토큰을 검증한 뒤, 성공하면 새로운 접근 토큰과 리프레시 토큰을 발행하여 그것들을 반환한다.
     * @param refreshTokenBase64 리프레시 토큰
     * @return 새로 발행된 접근 토큰과 리프레시 토큰이 담긴 {@link com.example.Flicktionary.global.utils.JwtUtils.TokenSet} 오브젝트
     */
    public JwtUtils.TokenSet createNewAccessTokenWithRefreshToken(String refreshTokenBase64) {
        UserAccount userAccount = getUserByRefreshToken(refreshTokenBase64);
        if (!isRefreshTokenFresh(userAccount) || !isRefreshTokenValid(userAccount, refreshTokenBase64)) {
            throw new RuntimeException("리프레시 토큰이 유효하지 않습니다.");
        }

        String newAccessToken = createNewAccessTokenWithClaims(userAccount.getUsername(), userAccount.getNickname());
        String newRefreshToken = rotateRefreshTokenOfUser(userAccount.getUsername());
        return new JwtUtils.TokenSet(newAccessToken, newRefreshToken);
    }

    /**
     * 접큰 토큰의 클레임에서 회원 정보를 추출한 뒤, 해당하는 회원의 엔티티 오브젝트를 반환한다.
     * @param token 접근 토큰
     * @return 접근 토큰이 발행된 회원의 엔티티 오브젝트
     */
    public UserAccount retrieveUserFromAccessToken(String token) {
        if (!isAccessTokenValid(token)) {
            throw new RuntimeException("접근 토큰이 유효하지 않습니다.");
        }
        Map<String, Object> claims = JwtUtils.getTokenPayload(secret, token);
        String username = (String) claims.get("username");
        return getUserByUsername(username);
    }

    /**
     * 주어진 유저 ID에 해당하는 회원의 리프레시 토큰을 재발급한다.
     * @param username 리프레시 토큰을 재발급할 회원의 유저 ID
     * @return 재발급된 리프레시 토큰
     */
    public String rotateRefreshTokenOfUser(String username) {
        UserAccount userAccount = getUserByUsername(username);
        userAccount.setRefreshToken(UuidUtils.uuidV4ToBase64String(UUID.randomUUID()));
        userAccount.setRefreshTokenExpiresAt(LocalDateTime.now().plusDays(refreshExpireDays));
        return userAccount.getRefreshToken();
    }

    /**
     * 주어진 접근 토큰의 유효성을 검증한다.
     * @param token 검증할 접근 토큰
     * @return 유효할시 {@code true}
     */
    private boolean isAccessTokenValid(String token) {
        return JwtUtils.isTokenValid(secret, token);
    }

    /**
     * 주어진 회원의 리프레시 토큰의 유효기간을 검증한다.
     * @param userAccount 리프레시 토큰의 유효기간을 검증할 회원
     * @return 유효한시 {@code true}
     */
    private boolean isRefreshTokenFresh(UserAccount userAccount) {
        return userAccount.getRefreshTokenExpiresAt().isAfter(LocalDateTime.now());
    }

    /**
     * 주어진 리프레시 토큰과 주어진 회원의 리프레시 토큰을 비교, 검증한다.
     * @param userAccount 리프레시 토큰의 소유자
     * @param refreshTokenBase64 검증하고자 하는 리프레시 토큰
     * @return 유효할시 {@code true}
     */
    private boolean isRefreshTokenValid(UserAccount userAccount, String refreshTokenBase64) {
        return userAccount.getRefreshToken().equals(refreshTokenBase64);
    }

    /**
     * 주어진 유저 ID에 해당하는 회원을 반환한다.
     * @param username 조회할 회원의 유저 ID
     * @return 회원 오브젝트
     */
    private UserAccount getUserByUsername(String username) {
        return userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * 주어진 리프레시 토큰을 소유하고 있는 회원을 반환한다.
     * @param refreshToken 조회할 회원의 리프레시 토큰
     * @return 회원 오브젝트
     */
    private UserAccount getUserByRefreshToken(String refreshToken) {
        return userAccountRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
