package com.novobanco.auth.infrastructure.web;

import com.novobanco.auth.domain.port.in.*;
import com.novobanco.auth.infrastructure.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "NovoBanco Auth Service")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshUseCase refreshUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    public AuthController(RegisterUseCase registerUseCase,
                          LoginUseCase loginUseCase,
                          RefreshUseCase refreshUseCase,
                          LogoutUseCase logoutUseCase,
                          GetCurrentUserUseCase getCurrentUserUseCase) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshUseCase = refreshUseCase;
        this.logoutUseCase = logoutUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterUseCase.Result result = registerUseCase.register(
                new RegisterUseCase.Command(request.email(), request.password(),
                        request.fullName(), request.customerId()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse(result.userId(), result.email(),
                        result.fullName(), result.customerId()));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT tokens")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginUseCase.Result result = loginUseCase.login(
                new LoginUseCase.Command(request.email(), request.password()));

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                result.user().userId(), result.user().email(),
                result.user().fullName(), result.user().customerId(),
                result.user().role());

        return ResponseEntity.ok(new LoginResponse(
                result.accessToken(), result.refreshToken(),
                result.expiresIn(), "Bearer", userInfo));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshUseCase.Result result = refreshUseCase.refresh(
                new RefreshUseCase.Command(request.refreshToken()));
        return ResponseEntity.ok(new RefreshResponse(result.accessToken(), result.expiresIn()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke tokens", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> logout(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        logoutUseCase.logout(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        GetCurrentUserUseCase.Result result = getCurrentUserUseCase.getCurrentUser(userId);
        return ResponseEntity.ok(new UserResponse(result.userId(), result.email(),
                result.fullName(), result.customerId(), result.role()));
    }
}
