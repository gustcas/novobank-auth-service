package com.novobanco.auth.domain.exception;

public class TokenRevokedException extends RuntimeException {
    public TokenRevokedException() {
        super("Refresh token has been revoked");
    }
}
