package com.novobanco.auth.domain.port.out;

import com.novobanco.auth.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void revokeAllByUserId(UUID userId);

    void deleteExpiredTokens();
}
