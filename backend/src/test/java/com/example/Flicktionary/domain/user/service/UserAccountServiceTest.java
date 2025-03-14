package com.example.Flicktionary.domain.user.service;

import com.example.Flicktionary.domain.user.dto.UserAccountDto;
import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("회원 서비스 테스트")
@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    private final UserAccountDto userAccountDto = new UserAccountDto(
            1L,
            "testUserDto",
            "testPasswordDto",
            "testDto@email.com",
            "testNicknameDto",
            "ADMIN"
    );

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAccountService userAccountService;

    @DisplayName("회원 정보가 담긴 DTO가 주어졌을때 회원을 가입시킨다.")
    @Test
    void givenDtoWhenRegisteringUserWillPersistUserAccount() {
        given(userAccountRepository.save(any(UserAccount.class)))
                .willReturn(userAccountDto.toEntity());
        given(passwordEncoder.encode(anyString())).willReturn(userAccountDto.password());

        UserAccountDto resUserAccountDto = userAccountService.registerUser(userAccountDto);

        assertNotNull(resUserAccountDto);
        assertEquals(userAccountDto.id(), resUserAccountDto.id());
        assertEquals(userAccountDto.username(), resUserAccountDto.username());
        assertEquals(userAccountDto.password(), resUserAccountDto.password());
        assertEquals(userAccountDto.email(), resUserAccountDto.email());
        assertEquals(userAccountDto.nickname(), resUserAccountDto.nickname());
        assertEquals(userAccountDto.role(), resUserAccountDto.role());
        then(userAccountRepository).should().save(any(UserAccount.class));
    }

    @DisplayName("존재하지 않는 회원의 정보를 수정할때 예외를 던진다.")
    @Test
    void givenNonexistentUserIdWhenModifyingUserThenThrowsException() {
        given(userAccountRepository.findById(0L)).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() ->
                userAccountService.modifyUser(0L, userAccountDto));

        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("유저를 찾을 수 없습니다.");
    }

    @DisplayName("회원의 정보를 수정한다.")
    @Test
    void givenUserIdWhenModifyingUserThenReturnsModifiedUserDto() {
        UserAccountDto newUserAccountDto = new UserAccountDto(
                999L,
                "newusername",
                "newpassword",
                "newemail@email.com",
                "newnickname",
                "USER"
        );
        given(userAccountRepository.findById(userAccountDto.id()))
                .willReturn(Optional.of(userAccountDto.toEntity()));
        given(userAccountRepository.save(any(UserAccount.class)))
                .willReturn(newUserAccountDto.toEntity());
        given(passwordEncoder.encode(anyString())).willReturn(newUserAccountDto.password());

        UserAccountDto result = userAccountService.modifyUser(
                userAccountDto.id(),
                newUserAccountDto
        );

        assertNotNull(result);
        assertEquals(newUserAccountDto.id(), result.id());
        assertEquals(newUserAccountDto.username(), result.username());
        assertEquals(newUserAccountDto.password(), result.password());
        assertEquals(newUserAccountDto.email(), result.email());
        assertEquals(newUserAccountDto.nickname(), result.nickname());
        assertEquals(newUserAccountDto.role(), result.role());
        then(userAccountRepository).should().save(any(UserAccount.class));
    }

    @DisplayName("고유 ID로 회원을 조회한다.")
    @Test
    void givenUserIdWhenFindingUserThenReturnsUser() {
        given(userAccountRepository.findById(userAccountDto.id()))
                .willReturn(Optional.of(userAccountDto.toEntity()));

        UserAccountDto result = userAccountService.getUserById(userAccountDto.id());

        assertNotNull(result);
        assertEquals(userAccountDto.id(), result.id());
        assertEquals(userAccountDto.username(), result.username());
        assertEquals(userAccountDto.password(), result.password());
        assertEquals(userAccountDto.email(), result.email());
        assertEquals(userAccountDto.nickname(), result.nickname());
        assertEquals(userAccountDto.role(), result.role());
    }

    @DisplayName("존재하지 않는 고유 ID로 회원을 조회할때 예외를 던진다.")
    @Test
    void givenNonexistentUserIdWhenFindingUserThenThrowsException() {
        given(userAccountRepository.findById(999L)).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> userAccountService.getUserById(999L));

        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("유저를 찾을 수 없습니다.");
    }

    @DisplayName("유저 ID로 회원을 조회한다.")
    @Test
    void givenUserUsernameWhenFindingUserThenReturnsUser() {
        given(userAccountRepository.findByUsername(userAccountDto.username()))
                .willReturn(Optional.of(userAccountDto.toEntity()));

        UserAccount result = userAccountService.getUserByUsername(userAccountDto.username());

        assertNotNull(result);
        assertEquals(userAccountDto.id(), result.getId());
        assertEquals(userAccountDto.username(), result.getUsername());
        assertEquals(userAccountDto.password(), result.getPassword());
        assertEquals(userAccountDto.email(), result.getEmail());
        assertEquals(userAccountDto.nickname(), result.getNickname());
        assertEquals(userAccountDto.role(), result.getRole().toString());
    }

    @DisplayName("존재하지 않는 유저 ID로 회원을 조회할때 예외를 던진다.")
    @Test
    void givenNonexistentUserUsernameWhenFindingUserThenThrowsException() {
        given(userAccountRepository.findByUsername("nonexistentuser"))
                .willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() ->
                userAccountService.getUserByUsername("nonexistentuser"));

        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("유저를 찾을 수 없습니다.");
    }

    @DisplayName("회원을 삭제한다.")
    @Test
    void givenUserIdWhenDeletingUserThenReturnsDeletedUserUsername() {
        given(userAccountRepository.findById(userAccountDto.id()))
                .willReturn(Optional.of(userAccountDto.toEntity()));

        String result = userAccountService.deleteUserById(userAccountDto.id());

        assertNotNull(result);
        assertEquals(userAccountDto.username(), result);
        then(userAccountRepository).should().delete(any(UserAccount.class));
    }

    @DisplayName("존재하지 않는 고유 ID로 회원을 삭제할때 예외를 던진다.")
    @Test
    void givenNonexistentUserIdWhenDeletingUserThenThrowsException() {
        given(userAccountRepository.findById(999L)).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() ->
                userAccountService.deleteUserById(999L));

        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("유저를 찾을 수 없습니다.");
    }
}