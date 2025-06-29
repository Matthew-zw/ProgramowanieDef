package com.example.projekt.exception;



public class UserAlreadyExistAuthenticationException extends RuntimeException {
    /**
     *
     * @param message
     */
    public UserAlreadyExistAuthenticationException(String message) {
        super(message);
    }
}