package com.example.Flicktionary.domain.user.service;

import com.example.Flicktionary.domain.user.dto.UserAccountDto;
import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.entity.UserAccountType;
import com.example.Flicktionary.domain.user.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 도메인에 해당하는 기능들을 지원하기 위한 서비스.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * 새 회원을 생성한 뒤 영속한다.
     * @param userAccountDto 새로 생성할 회원의 정보가 담긴 DTO
     * @return 영속된 회원에 해당하는 DTO
     */
    public UserAccountDto registerUser(UserAccountDto userAccountDto) {
        UserAccount userAccount = userAccountDto.toEntity();
        userAccount.setPassword(passwordEncoder.encode("{bcrypt}" + userAccount.getPassword()));
        return UserAccountDto.from(userAccountRepository.save(userAccount));
    }

    /**
     * 주어진 고유 ID에 해당하는 회원의 정보를 변경한다.
     * @param id 정보를 변경할 회원의 고유 ID
     * @param userAccountDto 새로 변경될 회원정보
     * @return 변경된 회원에 해당하는 DTO
     */
    public UserAccountDto modifyUser(Long id, UserAccountDto userAccountDto) {
        UserAccount userAccount = userAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        userAccount.setUsername(userAccountDto.username());
        userAccount.setPassword(passwordEncoder.encode("{bcrypt}" + userAccountDto.password()));
        userAccount.setEmail(userAccountDto.email());
        userAccount.setNickname(userAccountDto.nickname());
        userAccount.setRole(UserAccountType.valueOf(userAccountDto.role()));
        return UserAccountDto.from(userAccountRepository.save(userAccount));
    }

    /**
     * 주어진 고유 ID에 해당하는 회원을 반환한다.
     * @param id 조회할 회원의 고유 ID
     * @return 조회된 회원에 해당하는 DTO
     */
    public UserAccountDto getUserById(Long id) {
        return UserAccountDto.from(userAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다.")));
    }

    /**
     * 주어진 고유 ID에 해당하는 회원을 삭제한다.
     * @param id 삭제할 회원의 고유 ID
     * @return 삭제된 회원의 유저 ID
     */
    public String deleteUserById(Long id) {
        UserAccount userAccount = userAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        userAccountRepository.delete(userAccount);
        return userAccount.getUsername();
    }

    /**
     * 주어진 유저 ID에 해당하는 회원을 반환한다.
     * @param username 조회할 회원의 유저 ID
     * @return 조회된 회원 오브젝트
     */
    public UserAccount getUserByUsername(String username) {
        return userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
    }
}
