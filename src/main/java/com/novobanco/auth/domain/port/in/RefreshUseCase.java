package com.novobanco.auth.domain.port.in;

public interface RefreshUseCase {

    record Command(String refreshToken) {
    }

    record Result(String accessToken, long expiresIn) {
    }

    Result refresh(Command command);
}
