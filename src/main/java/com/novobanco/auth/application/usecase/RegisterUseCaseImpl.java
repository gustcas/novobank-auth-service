package com.novobanco.auth.application.usecase;

import com.novobanco.auth.domain.exception.EmailAlreadyExistsException;
import com.novobanco.auth.domain.model.User;
import com.novobanco.auth.domain.port.in.RegisterUseCase;
import com.novobanco.auth.domain.port.out.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class RegisterUseCaseImpl implements RegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUseCaseImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Result register(Command command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        User user = new User();
        user.setCustomerId(command.customerId());
        user.setEmail(command.email());
        user.setPassword(passwordEncoder.encode(command.password()));
        user.setFullName(command.fullName());
        user.setRole("CUSTOMER");
        user.setActive(true);
        user.setCreatedAt(OffsetDateTime.now());

        User saved = userRepository.save(user);

        return new Result(saved.getId(), saved.getEmail(), saved.getFullName(), saved.getCustomerId());
    }
}
