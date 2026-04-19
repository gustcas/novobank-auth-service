package com.novobanco.auth.application.usecase;

import com.novobanco.auth.domain.port.in.LogoutUseCase;
import com.novobanco.auth.domain.port.out.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LogoutUseCaseImpl implements LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutUseCaseImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
