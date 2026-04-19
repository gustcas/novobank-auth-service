package com.novobanco.auth.domain.port.in;

import java.util.UUID;

public interface GetCurrentUserUseCase {

    record Result(UUID userId, String email, String fullName, UUID customerId, String role) {
    }

    Result getCurrentUser(UUID userId);
}
