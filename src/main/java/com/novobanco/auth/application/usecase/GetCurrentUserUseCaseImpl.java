package com.novobanco.auth.application.usecase;

import com.novobanco.auth.domain.model.User;
import com.novobanco.auth.domain.port.in.GetCurrentUserUseCase;
import com.novobanco.auth.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetCurrentUserUseCaseImpl implements GetCurrentUserUseCase {

    private final UserRepository userRepository;

    public GetCurrentUserUseCaseImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Result getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        return new Result(user.getId(), user.getEmail(), user.getFullName(),
                user.getCustomerId(), user.getRole());
    }
}
