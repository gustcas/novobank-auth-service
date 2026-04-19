package com.novobanco.auth.unit;

import com.novobanco.auth.application.usecase.RegisterUseCaseImpl;
import com.novobanco.auth.domain.exception.EmailAlreadyExistsException;
import com.novobanco.auth.domain.model.User;
import com.novobanco.auth.domain.port.in.RegisterUseCase;
import com.novobanco.auth.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private RegisterUseCaseImpl registerUseCase;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(4); // low cost for tests
        registerUseCase = new RegisterUseCaseImpl(userRepository, passwordEncoder);
    }

    @Test
    void should_register_user_successfully_when_email_is_new() {
        UUID customerId = UUID.randomUUID();
        RegisterUseCase.Command command = new RegisterUseCase.Command(
                "new@novobanco.com", "Password123!", "New User", customerId);

        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        RegisterUseCase.Result result = registerUseCase.register(command);

        assertThat(result.email()).isEqualTo(command.email());
        assertThat(result.fullName()).isEqualTo(command.fullName());
        assertThat(result.customerId()).isEqualTo(customerId);
    }

    @Test
    void should_hash_password_with_bcrypt_before_saving() {
        RegisterUseCase.Command command = new RegisterUseCase.Command(
                "hash@novobanco.com", "PlainPassword!", "Hash Test", UUID.randomUUID());

        when(userRepository.existsByEmail(any())).thenReturn(false);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        registerUseCase.register(command);

        User saved = captor.getValue();
        assertThat(saved.getPassword()).isNotEqualTo("PlainPassword!");
        assertThat(passwordEncoder.matches("PlainPassword!", saved.getPassword())).isTrue();
    }

    @Test
    void should_throw_EmailAlreadyExistsException_when_email_is_duplicate() {
        RegisterUseCase.Command command = new RegisterUseCase.Command(
                "dup@novobanco.com", "Password123!", "Dup User", UUID.randomUUID());

        when(userRepository.existsByEmail(command.email())).thenReturn(true);

        assertThatThrownBy(() -> registerUseCase.register(command))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void should_never_return_password_in_response() {
        RegisterUseCase.Command command = new RegisterUseCase.Command(
                "safe@novobanco.com", "Secret123!", "Safe User", UUID.randomUUID());

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        RegisterUseCase.Result result = registerUseCase.register(command);

        // Result record has no password field — verified at compile time by the record definition
        assertThat(result).isNotNull();
        assertThat(result.toString()).doesNotContain("Secret123!");
    }
}
