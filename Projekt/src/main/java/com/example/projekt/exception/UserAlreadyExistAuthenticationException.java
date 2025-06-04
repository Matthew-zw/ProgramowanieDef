package com.example.projekt.exception;


// Możesz dziedziczyć po RuntimeException lub bardziej specyficznym wyjątku Spring Security, jeśli chcesz.
public class UserAlreadyExistAuthenticationException extends RuntimeException {
    public UserAlreadyExistAuthenticationException(String message) {
        super(message);
    }
}