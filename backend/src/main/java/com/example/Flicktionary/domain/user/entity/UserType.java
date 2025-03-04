package com.example.Flicktionary.domain.user.entity;

import lombok.Getter;

/**
 * 유저 계정의 분류를 나타내는 열거형.
 */
public enum UserType {
    USER("User"),
    ADMIN("Admin");

    /**
     * 각 열거형의 문자열 표현.
     */
    @Getter
    public final String description;

    UserType(String description) {
        this.description = description;
    }
}
