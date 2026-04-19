package com.novobanco.auth.domain.port.in;

import java.util.UUID;

public interface LogoutUseCase {

    void logout(UUID userId);
}
