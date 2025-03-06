package com.example.Flicktionary.domain.user.controller;

import com.example.Flicktionary.domain.user.dto.UserAccountDto;
import com.example.Flicktionary.domain.user.service.UserAccountJwtAuthenticationService;
import com.example.Flicktionary.domain.user.service.UserAccountService;
import com.example.Flicktionary.global.security.CustomUserDetails;
import com.example.Flicktionary.global.utils.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 도메인에 해당하는 API 엔드포인트
 */
@RequestMapping("/api/v1/users")
@Controller
@RequiredArgsConstructor
public class ApiV1UserAccountController {

    private final UserAccountService userAccountService;

    private final UserAccountJwtAuthenticationService userAccountJwtAuthenticationService;

    /**
     * 회원 가입
     * @param userAccountDto 요청 본문에서 파싱한 회원 정보 DTO
     * @return 영속된 회원 엔티티에 해당하는 DTO
     */
    @PostMapping("/register")
    public ResponseEntity<UserAccountDto> createUser(@RequestBody UserAccountDto userAccountDto) {
        return ResponseEntity.ok(userAccountService.registerUser(userAccountDto));
    }

    /**
     * 로그인하고자 하는 회원의 인증정보를 담는 DTO
     * @param username 회원의 유저 ID
     * @param password 회원의 비밀번호
     */
    record LoginRequest(
            String username,
            String password
    ) {
    }

    /**
     * 회원 로그인. 성공시 접근 토큰과 리프레시 토큰을 새로 생성한 뒤 쿠키로 반환한다.
     * @param loginRequest 로그인하고자 하는 회원의 인증정보
     * @param response
     * @return 성공 메시지 문자열을 답고 있는 ResponseEntity 오브젝트
     */
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Cookie accessToken = newCookieWithDefaultSettings("accessToken", userAccountJwtAuthenticationService.createNewAccessTokenForUser(loginRequest.username, loginRequest.password));
        Cookie refreshToken = newCookieWithDefaultSettings("refreshToken", userAccountJwtAuthenticationService.rotateRefreshTokenOfUser(loginRequest.username));
        response.addCookie(accessToken);
        response.addCookie(refreshToken);

        return ResponseEntity.ok("토큰이 성공적으로 발행되었습니다..");
    }

    /**
     * 회원 로그아웃. 접근 토큰과 리프레시 토큰을 담고 있는 쿠키에 빈 문자열을 할당한다.
     * @param response
     * @return 성공 메시지 문자열을 담고 있는 ResponseEntity 오브젝트
     */
    @GetMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletResponse response) {
        Cookie accessToken = newCookieWithDefaultSettings("accessToken", null);
        Cookie refreshToken = newCookieWithDefaultSettings("refreshToken", null);
        response.addCookie(accessToken);
        response.addCookie(refreshToken);

        return ResponseEntity.ok("쿠키가 성공적으로 비워졌습니다.");
    }

    /**
     * 접근 토큰 재생성. 쿠키로 리프레시 토큰을 받은 뒤, 검증이 통과하면 새 접근 토큰과 리프레시 토큰을 발행하여 쿠키로 반환한다.
     * @param refreshTokenBase64 base64로 인코딩된 리프레시 토큰 문자열
     * @param response
     * @return 성공 메시지 문자열을 담고 있는 ResponseEntity 오브젝트
     */
    @GetMapping("/refresh")
    public ResponseEntity<String> refreshAccessToken(@CookieValue("refreshToken") String refreshTokenBase64, HttpServletResponse response) {
        JwtUtils.TokenSet tokenSet = userAccountJwtAuthenticationService.createNewAccessTokenWithRefreshToken(refreshTokenBase64);
        Cookie accessToken = newCookieWithDefaultSettings("accessToken", tokenSet.access());
        Cookie refreshToken = newCookieWithDefaultSettings("refreshToken", tokenSet.refresh());
        response.addCookie(accessToken);
        response.addCookie(refreshToken);

        return ResponseEntity.ok("토큰이 성공적으로 재발행되었습니다.");
    }

    /**
     * 회원정보 수정. 수정할 회원의 고유 ID와 새 회원정보를 받아, 기존 회원의 가입정보를 덮어쓴다.
     * @param id 수정할 회원의 고유 ID
     * @param userAccountDto 새로 덮어씌워질 가입정보
     * @return 수정된 회원에 해당하는 DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserAccountDto> updateUser(@PathVariable Long id, @RequestBody UserAccountDto userAccountDto) {
        return ResponseEntity.ok(userAccountService.modifyUser(id, userAccountDto));
    }

    /**
     * 회원정보 조회. 조회할 회원의 고유 ID를 받아 해당 회원의 정보를 반환한다.
     * @param id 조회할 회원의 고유 ID
     * @return 조회된 회원에 해당하는 DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserAccountDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userAccountService.getUserById(id));
    }

    /**
     * 회원정보 조회. 현재 인증되어있는 회원의 정보를 반환한다.
     * @param principal 현재 인증되어 있는 회원의 정보
     * @return 조회된 회원에 해당하는 DTO
     */
    @GetMapping
    public ResponseEntity<UserAccountDto> getUserByPrinciple(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(UserAccountDto.from(userAccountService.getUserByUsername(principal.getUsername())));
    }

    /**
     * 회원 탈퇴. 탈퇴시킬 회원의 고유 ID를 받아 해당 회원을 탈퇴시킨다.
     * @param id 탈퇴할 회원의 고유 ID
     * @return 탈퇴된 회원의 유저 ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userAccountService.deleteUserById(id));
    }

    /**
     * 이름과 값을 지정해 새 쿠키 오브젝트를 생성한다.
     * @param name 쿠키의 이름
     * @param value 쿠키의 값
     * @return 새 쿠키 오브젝트
     */
    private Cookie newCookieWithDefaultSettings(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setDomain("localhost");
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }
}
