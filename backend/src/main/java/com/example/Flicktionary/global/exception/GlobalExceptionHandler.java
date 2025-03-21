package com.example.Flicktionary.global.exception;

import com.example.Flicktionary.global.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // ServiceException 처리
    @ResponseStatus
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ResponseDto<Void>> ServiceExceptionHandler(ServiceException e) {
        return ResponseEntity
                .status(e.getCode())
                .body(ResponseDto.of(e.getCode() + "", e.getMessage(), null));
    }

    // 그 외 모든 예외 처리
    @ResponseStatus
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Void>> ExceptionHandler(Exception e) {
        int code = HttpStatus.INTERNAL_SERVER_ERROR.value();
        e.printStackTrace();
        return ResponseEntity
                .status(code)
                .body(ResponseDto.of(code + "", "서버 에러가 발생했습니다.", null));
    }
}
