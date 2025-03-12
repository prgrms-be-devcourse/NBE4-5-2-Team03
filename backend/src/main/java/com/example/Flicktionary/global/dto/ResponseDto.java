package com.example.Flicktionary.global.dto;

import org.springframework.http.HttpStatus;

/**
 * HTTP 응답 DTO.
 * @param code 응답 코드
 * @param message 응답 메시지
 * @param data 응답 본문
 * @param <T> 본문의 타입
 */
public record ResponseDto<T>(
        String code,
        String message,
        T data
) {
    /**
     * 응답 본문이 없는 경우의 생성자.
     * @param code
     * @param message
     */
    public ResponseDto(String code, String message) {
        this(code, message, null);
    }

    /**
     * 응답 본문이 없는 경우의 생성자 메소드.
     * @param code
     * @param message
     */
    public static ResponseDto<?> of(String code, String message) {
        return new ResponseDto<>(code, message);
    }

    /**
     * 생성자 메소드.
     * @param code
     * @param message
     * @param data
     * @return
     * @param <T>
     */
    public static <T> ResponseDto<T> of(String code, String message, T data) {
        return new ResponseDto<>(code, message, data);
    }

    /**
     * HTTP 응답 코드 200 OK에 해당하는 응답을 반환한다.
     * @param data 응답 본문
     * @return
     * @param <T> 본문의 타입
     */
    public static <T> ResponseDto<T> ok(String message, T data) {
        return new ResponseDto<>(HttpStatus.OK.value() + "", message, data);
    }

    public static <T> ResponseDto<T> ok(T data) {
        return new ResponseDto<>(HttpStatus.OK.value() + "", "정상 처리되었습니다.", data);
    }

    public static ResponseDto<?> ok(String message) {
        return new ResponseDto<>(HttpStatus.OK.value() + "", message);
    }
}
