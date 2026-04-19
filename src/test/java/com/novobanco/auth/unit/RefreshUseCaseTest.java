package com.novobanco.auth.unit;

import com.novobanco.auth.application.usecase.LoginUseCaseImpl;
import com.novobanco.auth.application.usecase.RefreshUseCaseImpl;
import com.novobanco.auth.domain.exception.TokenExpiredException;
import com.novobanco.auth.domain.exception.TokenNotFoundException;
import com.novobanco.auth.domain.exception.TokenRevokedException;
import com.novobanco.auth.domain.model.RefreshToken;
import com.novobanco.auth.domain.model.User;
import com.novobanco.auth.domain.port.in.RefreshUseCase;
import com.novobanco.auth.domain.port.out.RefreshTokenRepository;
import com.novobanco.auth.domain.port.out.UserRepository;
import com.novobanco.auth.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshUseCaseTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;

    private RefreshUseCaseImpl refreshUseCase;
    private String rawToken;
    private String tokenHash;

    @BeforeEach
    void setUp() {
        refreshUseCase = new RefreshUseCaseImpl(refreshTokenRepository, userRepository, jwtService);
        rawToken = UUID.randomUUID().toString();
        tokenHash = LoginUseCaseImpl.sha256(rawToken);
    }

    @Test
    void should_return_new_access_token_when_refresh_token_is_valid() {
        UUID userId = UUID.randomUUID();
        RefreshToken stored = validToken(userId);
        User user = buildUser(userId);

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(stored));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("new.access.token");
        when(jwtService.getExpirationMs()).thenReturn(900_000L);

        RefreshUseCase.Result result = refreshUseCase.refresh(new RefreshUseCase.Command(rawToken));

        assertThat(result.accessToken()).isEqualTo("new.access.token");
        assertThat(result.expiresIn()).isEqualTo(900L);
    }

    @Test
    void should_throw_exception_when_refresh_token_is_revoked() {
        UUID userId = UUID.randomUUID();
        RefreshToken revoked = validToken(userId);
        revoked.setRevoked(true);

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> refreshUseCase.refresh(new RefreshUseCase.Command(rawToken)))
                .isInstanceOf(TokenRevokedException.class);
    }

    @Test
    void should_throw_exception_when_refresh_token_is_expired() {
        UUID userId = UUID.randomUUID();
        RefreshToken expired = new RefreshToken(UUID.randomUUID(), userId, tokenHash,
                OffsetDateTime.now().minusDays(1), false, OffsetDateTime.now().minusDays(8));

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> refreshUseCase.refresh(new RefreshUseCase.Command(rawToken)))
                .isInstanceOf(TokenExpiredException.class);
    }

    @Test
    void should_throw_exception_when_refresh_token_not_found() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshUseCase.refresh(new RefreshUseCase.Command(rawToken)))
                .isInstanceOf(TokenNotFoundException.class);
    }

    private RefreshToken validToken(UUID userId) {
        return new RefreshToken(UUID.randomUUID(), userId, tokenHash,
                OffsetDateTime.now().plusDays(7), false, OffsetDateTime.now());
    }

    private User buildUser(UUID userId) {
        User u = new User();
        u.setId(userId);
        u.setCustomerId(UUID.randomUUID());
        u.setEmail("user@novobanco.com");
        u.setRole("CUSTOMER");
        u.setActive(true);
        return u;
    }
}
