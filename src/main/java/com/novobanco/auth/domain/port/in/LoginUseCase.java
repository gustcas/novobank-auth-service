package com.novobanco.auth.domain.port.in;

import java.util.UUID;

public interface LoginUseCase {

    record Command(String email, String password) {
    }

    record UserInfo(UUID userId, String email, String fullName, UUID customerId, String role) {
    }

    record Result(String accessToken, String refreshToken, long expiresIn, UserInfo user) {
    }

    Result login(Command command);
}
