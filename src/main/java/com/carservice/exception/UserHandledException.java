package com.carservice.exception;

import org.springframework.http.HttpStatus;

public class UserHandledException extends Throwable {

    private HttpStatus httpStatus;

    public UserHandledException(String s, HttpStatus status) {
        super(s);
        this.httpStatus = status;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
