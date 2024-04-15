package com.ss871104.oauth2security.util;

import org.springframework.http.HttpStatus;

public class ServiceErrorException extends Exception {
    private final HttpStatus code;
    public ServiceErrorException(String msg, HttpStatus code) {
        super(msg);
        this.code = code;
    }

    public HttpStatus getCode() {
        return code;
    }
}
