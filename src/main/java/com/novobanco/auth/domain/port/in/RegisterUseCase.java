package com.novobanco.auth.domain.port.in;

import java.util.UUID;

public interface RegisterUseCase {

    record Command(String email, String password, String fullName, UUID customerId) {
    }

    record Result(UUID userId, String email, String fullName, UUID customerId) {
    }

    Result register(Command command);
}
