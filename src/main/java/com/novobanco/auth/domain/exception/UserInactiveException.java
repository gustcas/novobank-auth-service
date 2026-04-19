package com.novobanco.auth.domain.exception;

public class UserInactiveException extends RuntimeException {
    public UserInactiveException() {
        super("User account is inactive");
    }
}
