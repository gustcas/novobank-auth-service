package com.novobanco.auth.infrastructure.web.dto;

import java.util.UUID;

public record RegisterResponse(UUID userId, String email, String fullName, UUID customerId) {
}
