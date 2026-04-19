package com.novobanco.auth.application.usecase;

import com.novobanco.auth.domain.exception.InvalidCredentialsException;
import com.novobanco.auth.domain.exception.UserInactiveException;
import com.novobanco.auth.domain.model.RefreshToken;
import com.novobanco.auth.domain.model.User;
import com.novobanco.auth.domain.port.in.LoginUseCase;
import com.novobanco.auth.domain.port.out.RefreshTokenRepository;
import com.novobanco.auth.domain.port.out.UserRepository;
import com.novobanco.auth.infrastructure.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class LoginUseCaseImpl implements LoginUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final long refreshExpirationMs;

    public LoginUseCaseImpl(UserRepository userRepository,
                            RefreshTokenRepository refreshTokenRepository,
                            JwtService jwtService,
                            PasswordEncoder passwordEncoder,
                            @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Override
    public Result login(Command command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        if (!user.isActive()) {
            throw new UserInactiveException();
        }

        user.setLastLogin(OffsetDateTime.now());
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenHash(sha256(rawRefreshToken));
        refreshToken.setExpiresAt(OffsetDateTime.now().plusSeconds(refreshExpirationMs / 1000));
        refreshToken.setRevoked(false);
        refreshToken.setCreatedAt(OffsetDateTime.now());
        refreshTokenRepository.save(refreshToken);

        UserInfo userInfo = new UserInfo(
                user.getId(), user.getEmail(), user.getFullName(),
                user.getCustomerId(), user.getRole());

        return new Result(accessToken, rawRefreshToken, jwtService.getExpirationMs() / 1000, userInfo);
    }

    static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
