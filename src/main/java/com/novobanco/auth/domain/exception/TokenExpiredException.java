package com.novobanco.auth.domain.exception;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException() {
        super("Refresh token has expired");
    }
}
