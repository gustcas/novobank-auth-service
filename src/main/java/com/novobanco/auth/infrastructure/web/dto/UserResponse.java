package com.novobanco.auth.infrastructure.web.dto;

import java.util.UUID;

public record UserResponse(UUID userId, String email, String fullName, UUID customerId, String role) {
}
