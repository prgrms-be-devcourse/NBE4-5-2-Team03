package com.example.Flicktionary.domain.user.controller;

import com.example.Flicktionary.domain.user.dto.UserAccountDto;
import com.example.Flicktionary.domain.user.service.UserAccountJwtAuthenticationService;
import com.example.Flicktionary.domain.user.service.UserAccountService;
import com.example.Flicktionary.global.security.CustomUserDetailsService;
import com.example.Flicktionary.global.utils.JwtUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("회원 도메인 컨트롤러 테스트")
// 퍼포먼스를 위해 필요한 모듈만 초기화
@Import({UserAccountService.class,
        UserAccountJwtAuthenticationService.class,
        CustomUserDetailsService.class})
@WebMvcTest(UserAccountController.class)
// 테스트 실행 시 Spring Security 비활성화
@AutoConfigureMockMvc(addFilters = false)
class UserAccountControllerTest {

    @Autowired
    private final MockMvc mockMvc;

    @MockitoBean
    private UserAccountService userAccountService;

    @MockitoBean
    private UserAccountJwtAuthenticationService userAccountJwtAuthenticationService;


    public UserAccountControllerTest(@Autowired MockMvc mockMvc,
                                     @Autowired UserAccountService userAccountService,
                                     @Autowired UserAccountJwtAuthenticationService userAccountJwtAuthenticationService) {
        this.mockMvc = mockMvc;
        this.userAccountService = userAccountService;
        this.userAccountJwtAuthenticationService = userAccountJwtAuthenticationService;
    }

    @DisplayName("회원 가입을 한다.")
    @Test
    void givenValidDtoWhenRegisteringUserThenRegistersUser() throws Exception {
        given(userAccountService.registerUser(any(UserAccountDto.class))).willReturn(new UserAccountDto(
                1L,
                "testUsername",
                "testPassword",
                "test@email.com",
                "testNickname",
                "USER"
        ));

        mockMvc.perform(
                post("/api/users/register")
                        // POST 요청 본문이 비어있으면 예외가 발생하므로 임의의 문자열을 첨부
                        .content("{\"fake\": \"json\"}")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.username").value("testUsername"))
                .andExpect(jsonPath("$.password").value("testPassword"))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.nickname").value("testNickname"))
                .andExpect(jsonPath("$.role").value("USER"));

        then(userAccountService).should().registerUser(any(UserAccountDto.class));
    }

    @DisplayName("로그인 요청을 받으면 새 접근 토큰과 리프레시 토큰을 쿠키로 발행한다.")
    @Test
    void givenUserCredentialsWhenLoggingInThenReturnsNewTokens() throws Exception {
        String username = "testUsername";
        String password = "testPassword";
        given(userAccountJwtAuthenticationService
                .createNewAccessTokenForUser(username, password))
                .willReturn("fakeAccessToken");
        given(userAccountJwtAuthenticationService
                .rotateRefreshTokenOfUser(username))
                .willReturn("fakeRefreshToken");

        mockMvc.perform(
                        post("/api/users/login")
                                .content("{\"username\": \"testUsername\", \"password\": \"testPassword\"}")
                                .contentType("application/json")
                                .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                // TODO: 매번 쿠키 설정을 assert하는 대신 전용 테스트 케이스로 분리. 관련 메소드가 private이기 때문에 UserAccountController.logoutUser 메소드를 사용하면 될 듯.
                .andExpect(cookie().value("accessToken", "fakeAccessToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().maxAge("accessToken", 25920000))
                .andExpect(cookie().value("refreshToken", "fakeRefreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().maxAge("refreshToken", 25920000))
                .andExpect(content().string("토큰이 성공적으로 발행되었습니다."));

        then(userAccountJwtAuthenticationService).should()
                .createNewAccessTokenForUser(username, password);
        then(userAccountJwtAuthenticationService).should()
                .rotateRefreshTokenOfUser(username);
    }

    @DisplayName("로그아웃 요청을 받으면 토큰 쿠키를 비운다.")
    @Test
    void givenNothingWhenLoggingOutThenReturnsEmptyTokens() throws Exception {
        mockMvc.perform(
                        get("/api/users/logout")
                                .cookie(new Cookie("refreshToken", "faketoken")))
                .andExpect(status().isOk())
                .andExpect(cookie().value("accessToken", ""))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().maxAge("accessToken", 25920000))
                .andExpect(cookie().value("refreshToken", ""))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().maxAge("refreshToken", 25920000))
                .andExpect(content().string("쿠키가 성공적으로 비워졌습니다."));
    }

    @DisplayName("리프레시 토큰으로 접근 토큰을 쿠키로 재발행한다.")
    @Test
    void givenRefreshTokenWhenRequestingFreshAccessTokenThenReturnsNewTokens() throws Exception {
        given(userAccountJwtAuthenticationService
                .createNewAccessTokenWithRefreshToken("faketoken"))
                .willReturn(new JwtUtils.TokenSet("fakeAccessToken", "fakeRefreshToken"));

        mockMvc.perform(
                        get("/api/users/refresh")
                                .cookie(new Cookie("refreshToken", "faketoken")))
                .andExpect(status().isOk())
                .andExpect(cookie().value("accessToken", "fakeAccessToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().maxAge("accessToken", 25920000))
                .andExpect(cookie().value("refreshToken", "fakeRefreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().maxAge("refreshToken", 25920000))
                .andExpect(content().string("토큰이 성공적으로 재발행되었습니다."));

        then(userAccountJwtAuthenticationService).should()
                .createNewAccessTokenWithRefreshToken(anyString());
    }

    @DisplayName("인증 정보가 있는 클라이언트에게 로그인 상태 확인 요청을 받으면, 인등 정보가 있다고 응답한다.")
    @Test
    void givenCredentialsWhenRequestingCredentialStatusThenReturnOk() throws Exception {
        mockMvc.perform(
                get("/api/users/status")
                        .cookie(new Cookie("accessToken", "faketoken"),
                                new Cookie("refreshToken", "faketoken")))
                .andExpect(status().isOk())
                .andExpect(content().string("인증 정보가 존재합니다."));
    }

    @DisplayName("인증 정보가 없는 클라이언트에게 로그인 상태 확인 요청을 받으면, 인등 정보가 없다고 응답한다.")
    @Test
    void givenNoCredentialsWhenRequestingCredentialStatusThenReturnForbidden() throws Exception {
        mockMvc.perform(
                        get("/api/users/status"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("인증 정보가 존재하지 않습니다."));
    }
}