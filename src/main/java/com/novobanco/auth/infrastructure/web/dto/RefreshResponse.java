package com.novobanco.auth.infrastructure.web.dto;

public record RefreshResponse(String accessToken, long expiresIn) {
}
