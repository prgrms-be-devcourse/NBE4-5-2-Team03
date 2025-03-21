package com.example.Flicktionary.global.exception;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {
    private final int code;

    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }
}
