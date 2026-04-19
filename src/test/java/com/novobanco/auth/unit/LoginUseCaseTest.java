package com.novobanco.auth.unit;

import com.novobanco.auth.application.usecase.LoginUseCaseImpl;
import com.novobanco.auth.domain.exception.InvalidCredentialsException;
import com.novobanco.auth.domain.exception.UserInactiveException;
import com.novobanco.auth.domain.model.RefreshToken;
import com.novobanco.auth.domain.model.User;
import com.novobanco.auth.domain.port.in.LoginUseCase;
import com.novobanco.auth.domain.port.out.RefreshTokenRepository;
import com.novobanco.auth.domain.port.out.UserRepository;
import com.novobanco.auth.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtService jwtService;

    private PasswordEncoder passwordEncoder;
    private LoginUseCaseImpl loginUseCase;
    private User activeUser;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(4);
        loginUseCase = new LoginUseCaseImpl(userRepository, refreshTokenRepository,
                jwtService, passwordEncoder, 604_800_000L);

        activeUser = new User();
        activeUser.setId(UUID.randomUUID());
        activeUser.setCustomerId(UUID.randomUUID());
        activeUser.setEmail("test@novobanco.com");
        activeUser.setPassword(passwordEncoder.encode("Password123!"));
        activeUser.setFullName("Test User");
        activeUser.setRole("CUSTOMER");
        activeUser.setActive(true);
        activeUser.setCreatedAt(OffsetDateTime.now());
    }

    @Test
    void should_return_tokens_when_credentials_are_valid() {
        when(userRepository.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));
        when(userRepository.save(any())).thenReturn(activeUser);
        when(jwtService.generateAccessToken(any())).thenReturn("access.token.value");
        when(jwtService.getExpirationMs()).thenReturn(900_000L);
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoginUseCase.Result result = loginUseCase.login(
                new LoginUseCase.Command(activeUser.getEmail(), "Password123!"));

        assertThat(result.accessToken()).isEqualTo("access.token.value");
        assertThat(result.refreshToken()).isNotBlank();
    }

    @Test
    void should_update_last_login_timestamp_on_successful_login() {
        when(userRepository.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenReturn(activeUser);
        when(jwtService.generateAccessToken(any())).thenReturn("token");
        when(jwtService.getExpirationMs()).thenReturn(900_000L);
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        loginUseCase.login(new LoginUseCase.Command(activeUser.getEmail(), "Password123!"));

        assertThat(captor.getValue().getLastLogin()).isNotNull();
    }

    @Test
    void should_throw_InvalidCredentialsException_when_password_is_wrong() {
        when(userRepository.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> loginUseCase.login(
                new LoginUseCase.Command(activeUser.getEmail(), "WrongPassword!")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void should_throw_InvalidCredentialsException_when_email_not_found() {
        when(userRepository.findByEmail("noone@novobanco.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.login(
                new LoginUseCase.Command("noone@novobanco.com", "Password123!")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void should_throw_UserInactiveException_when_user_is_inactive() {
        activeUser.setActive(false);
        when(userRepository.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> loginUseCase.login(
                new LoginUseCase.Command(activeUser.getEmail(), "Password123!")))
                .isInstanceOf(UserInactiveException.class);
    }

    @Test
    void should_save_hashed_refresh_token_never_plain_text() {
        when(userRepository.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));
        when(userRepository.save(any())).thenReturn(activeUser);
        when(jwtService.generateAccessToken(any())).thenReturn("token");
        when(jwtService.getExpirationMs()).thenReturn(900_000L);
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        when(refreshTokenRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        LoginUseCase.Result result = loginUseCase.login(
                new LoginUseCase.Command(activeUser.getEmail(), "Password123!"));

        RefreshToken stored = captor.getValue();
        assertThat(stored.getTokenHash()).isNotEqualTo(result.refreshToken());
        assertThat(stored.getTokenHash()).hasSize(64); // SHA-256 hex = 64 chars
    }
}
