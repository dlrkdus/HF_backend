package com.hf.healthfriend.domain.wish.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.hf.healthfriend.domain.wish")
public class WishErrorControllerAdvice {
    @ExceptionHandler(value = WishException.class)
    protected ResponseEntity<WishErrorResponse> wishException(WishException e) {
        WishErrorResponse response = WishErrorResponse.of(e.getErrorCode(),e.getDetails());
        response.setMessage(e.getMessage());
        response.setDetail(e.getDetails());
        return new ResponseEntity<>(response, e.getHttpStatus());
    }
}
