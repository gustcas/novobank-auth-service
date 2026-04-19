package com.novobanco.auth.infrastructure.web.dto;

import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType,
        UserInfo user
) {
    public record UserInfo(UUID userId, String email, String fullName, UUID customerId, String role) {
    }
}
