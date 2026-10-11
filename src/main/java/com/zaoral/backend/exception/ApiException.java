package com.zaoral.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * 想定済みのエラー。指定したステータスとメッセージでそのままレスポンスになる。
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

}
