package com.novobanco.auth.application.usecase;

import com.novobanco.auth.domain.exception.TokenExpiredException;
import com.novobanco.auth.domain.exception.TokenNotFoundException;
import com.novobanco.auth.domain.exception.TokenRevokedException;
import com.novobanco.auth.domain.model.RefreshToken;
import com.novobanco.auth.domain.model.User;
import com.novobanco.auth.domain.port.in.RefreshUseCase;
import com.novobanco.auth.domain.port.out.RefreshTokenRepository;
import com.novobanco.auth.domain.port.out.UserRepository;
import com.novobanco.auth.infrastructure.security.JwtService;
import org.springframework.stereotype.Service;

@Service
public class RefreshUseCaseImpl implements RefreshUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public RefreshUseCaseImpl(RefreshTokenRepository refreshTokenRepository,
                              UserRepository userRepository,
                              JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public Result refresh(Command command) {
        String hash = LoginUseCaseImpl.sha256(command.refreshToken());

        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(TokenNotFoundException::new);

        if (stored.isRevoked()) {
            throw new TokenRevokedException();
        }
        if (stored.isExpired()) {
            throw new TokenExpiredException();
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(TokenNotFoundException::new);

        String newAccessToken = jwtService.generateAccessToken(user);
        return new Result(newAccessToken, jwtService.getExpirationMs() / 1000);
    }
}
