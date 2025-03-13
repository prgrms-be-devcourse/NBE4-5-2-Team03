package com.example.Flicktionary.domain.review.controller;

import com.example.Flicktionary.global.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class ReviewExceptionHandler {

    // NoSuchElementException 처리
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ResponseDto<?>> handleNoSuchElementException(NoSuchElementException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseDto.of(HttpStatus.NOT_FOUND.value() + "", "찾을 수 없습니다.", null)); // 또는 상세 메시지
    }

    // IllegalArgumentException 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDto<?>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseDto.of(HttpStatus.BAD_REQUEST.value() + "", "잘못된 요청입니다.", null)); // 또는 상세 메시지
    }

    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<?>> handleException(Exception e) {
        System.err.println("서버 오류 발생: " + e.getMessage()); // 로그 기록

        // 리뷰를 이미 작성한 경우
        if (e.getMessage().equals("이미 리뷰를 작성하셨습니다.")) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ResponseDto.of(HttpStatus.CONFLICT.value() + "",
                            e.getMessage(), null));
        }

        // 이외의 오류 로그
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.of(HttpStatus.INTERNAL_SERVER_ERROR.value() + "", "서버 오류가 발생했습니다.", null)); // 일반적인 오류 메시지
    }
}
