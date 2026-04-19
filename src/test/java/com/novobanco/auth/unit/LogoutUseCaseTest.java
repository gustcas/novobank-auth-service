package com.novobanco.auth.unit;

import com.novobanco.auth.application.usecase.LogoutUseCaseImpl;
import com.novobanco.auth.domain.port.out.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private LogoutUseCaseImpl logoutUseCase;

    @BeforeEach
    void setUp() {
        logoutUseCase = new LogoutUseCaseImpl(refreshTokenRepository);
    }

    @Test
    void should_revoke_all_active_refresh_tokens_for_user() {
        UUID userId = UUID.randomUUID();

        logoutUseCase.logout(userId);

        verify(refreshTokenRepository, times(1)).revokeAllByUserId(userId);
    }

    @Test
    void should_not_fail_when_user_has_no_active_tokens() {
        UUID userId = UUID.randomUUID();
        doNothing().when(refreshTokenRepository).revokeAllByUserId(userId);

        logoutUseCase.logout(userId);

        verify(refreshTokenRepository).revokeAllByUserId(userId);
    }
}
