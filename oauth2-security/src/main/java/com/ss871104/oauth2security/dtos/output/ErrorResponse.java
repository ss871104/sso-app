package com.ss871104.oauth2security.dtos.output;

import org.springframework.http.HttpStatus;

public class ErrorResponse {
    private final HttpStatus status;
    private final String message;

    public ErrorResponse(HttpStatus status, String message) {
        this.status= status;
        this.message = message;
    }
}
