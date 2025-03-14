package com.example.Flicktionary.domain.user.service;

import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.entity.UserAccountType;
import com.example.Flicktionary.domain.user.repository.UserAccountRepository;
import com.example.Flicktionary.global.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("회원 JWT 인증 서비스 테스트")
@ExtendWith(MockitoExtension.class)
class UserAccountJwtAuthenticationServiceTest {

    private final UserAccount userAccount = new UserAccount(
            1L,
            "testUserAccount",
            "testUserAccountPassword",
            "test@email.com",
            "testNickname",
            UserAccountType.ADMIN
    );

    private final String testSecret = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890";

    private final Integer testAccessExpireSeconds = 60;

    private final Integer testRefreshExpireDays = 3;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAccountJwtAuthenticationService userAccountJwtAuthenticationService;

    @BeforeEach
    void injectAutowiredValues() {
        ReflectionTestUtils.setField(
                userAccountJwtAuthenticationService,
                "secret",
                testSecret);
        ReflectionTestUtils.setField(
                userAccountJwtAuthenticationService,
                "accessExpireSeconds",
                testAccessExpireSeconds);
        ReflectionTestUtils.setField(
                userAccountJwtAuthenticationService,
                "refreshExpireDays",
                testRefreshExpireDays);
    }

    @DisplayName("올바른 인증 정보가 주어졌을때 해당 유저에게 접근 토큰을 발행한다.")
    @Test
    void givenCorrectUserCredentialsWhenCreatingTokenThenReturnTokenWithCorrectClaims() {
        given(userAccountRepository.findByUsername(anyString())).willReturn(Optional.of(userAccount));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

        String token = userAccountJwtAuthenticationService.createNewAccessTokenForUser(
                userAccount.getUsername(),
                userAccount.getPassword());
        Map<String, Object> claims = JwtUtils.getTokenPayload(testSecret, token);

        assertTrue(claims.containsKey("username"));
        assertTrue(claims.containsKey("nickname"));
        assertEquals(userAccount.getUsername(), claims.get("username"));
        assertEquals(userAccount.getNickname(), claims.get("nickname"));
        then(userAccountRepository).should().findByUsername(userAccount.getUsername());
    }

    @DisplayName("올바르지 않은 인증 정보가 주어졌을때 예외를 던진다.")
    @Test
    void givenIncorrectUserCredentialsWhenCreatingTokenThenThrowException() {
        given(userAccountRepository.findByUsername(userAccount.getUsername())).willReturn(Optional.of(userAccount));

        Throwable thrown = catchThrowable(() -> userAccountJwtAuthenticationService.createNewAccessTokenForUser(
                userAccount.getUsername(),
                "wrongPassword"));

        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("비밀번호가 틀립니다.");
    }

    @DisplayName("올바른 리프레시 토큰이 주어졌을때 접근 토큰와 리프레시 토큰을 재발행한다.")
    @Test
    void givenValidRefreshTokenWhenRefreshingTokensThenReturnRefreshedTokens() {
        given(userAccountRepository.findByRefreshToken(anyString())).willReturn(Optional.of(userAccount));
        given(userAccountRepository.findByUsername(anyString())).willReturn(Optional.of(userAccount));

        String fakeRefreshToken = "=#=#=#=#=#=#=#=#=#=#=#=#";
        LocalDateTime fakeExpiryDate = LocalDateTime.of(3000, 1, 1, 0, 0);
        userAccount.setRefreshToken(fakeRefreshToken);
        userAccount.setRefreshTokenExpiresAt(fakeExpiryDate);

        JwtUtils.TokenSet tokens = userAccountJwtAuthenticationService
                .createNewAccessTokenWithRefreshToken(userAccount.getRefreshToken());

        assertNotEquals(fakeRefreshToken, userAccount.getRefreshToken());
        assertNotEquals(fakeExpiryDate, userAccount.getRefreshTokenExpiresAt());
        assertThat(tokens.access()).isNotEmpty();
        then(userAccountRepository).should().findByRefreshToken(fakeRefreshToken);
        then(userAccountRepository).should().findByUsername(userAccount.getUsername());
    }

    @DisplayName("만료된 리프레시 토큰이 주어졌을때 예외를 던진다.")
    @Test
    void givenStaleRefreshTokenWhenRefreshingTokensThenThrowException() {
        given(userAccountRepository.findByRefreshToken(anyString())).willReturn(Optional.of(userAccount));

        String wrongRefreshToken = "=#=#=#=#=#=#=#=#=#=#=#=#";
        LocalDateTime fakeExpiryDate = LocalDateTime.of(3000, 1, 1, 0, 0);
        userAccount.setRefreshToken("fakeRefreshToken");
        userAccount.setRefreshTokenExpiresAt(fakeExpiryDate);

        Throwable thrown = catchThrowable(() -> userAccountJwtAuthenticationService
                .createNewAccessTokenWithRefreshToken(wrongRefreshToken));

        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("리프레시 토큰이 유효하지 않습니다.");
        then(userAccountRepository).should().findByRefreshToken(wrongRefreshToken);
    }
}