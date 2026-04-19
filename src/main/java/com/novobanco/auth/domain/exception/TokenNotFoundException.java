package com.novobanco.auth.domain.exception;

public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException() {
        super("Refresh token not found");
    }
}
