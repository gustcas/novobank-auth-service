package com.novobanco.auth.unit;

import com.novobanco.auth.domain.model.User;
import com.novobanco.auth.infrastructure.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "dGVzdF9zZWNyZXRfbWluaW11bV8yNTZfYml0c19mb3JfdW5pdF90ZXN0c19vbmx5"; // base64
    private static final long EXPIRATION_MS = 900_000L; // 15 min

    private JwtService jwtService;
    private User validUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MS);

        validUser = new User();
        validUser.setId(UUID.randomUUID());
        validUser.setCustomerId(UUID.randomUUID());
        validUser.setEmail("test@novobanco.com");
        validUser.setFullName("Test User");
        validUser.setRole("CUSTOMER");
        validUser.setActive(true);
    }

    @Test
    void should_generate_valid_token_when_user_is_valid() {
        String token = jwtService.generateAccessToken(validUser);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void should_extract_claims_correctly_when_token_is_valid() {
        String token = jwtService.generateAccessToken(validUser);

        Claims claims = jwtService.extractAllClaims(token);

        assertThat(claims.getSubject()).isEqualTo(validUser.getId().toString());
        assertThat(claims.get("email", String.class)).isEqualTo(validUser.getEmail());
        assertThat(claims.get("customerId", String.class)).isEqualTo(validUser.getCustomerId().toString());
        assertThat(claims.get("role", String.class)).isEqualTo("CUSTOMER");
    }

    @Test
    void should_return_true_when_token_is_not_expired() {
        String token = jwtService.generateAccessToken(validUser);

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void should_throw_exception_when_token_is_expired() {
        JwtService shortLivedService = new JwtService(SECRET, -1000L); // already expired
        String token = shortLivedService.generateAccessToken(validUser);

        assertThatThrownBy(() -> jwtService.extractAllClaims(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void should_throw_exception_when_token_is_malformed() {
        assertThatThrownBy(() -> jwtService.extractAllClaims("not.a.jwt.token"))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    void should_throw_exception_when_signature_is_invalid() {
        String token = jwtService.generateAccessToken(validUser);
        // tamper with the signature
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "invalidsig";

        assertThatThrownBy(() -> jwtService.extractAllClaims(tampered))
                .isInstanceOf(SignatureException.class);
    }
}
