package com.novobanco.auth.infrastructure.web;

import com.novobanco.auth.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String BASE_TYPE = "https://api.novobanco.com/errors";

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailDuplicate(EmailAlreadyExistsException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setType(URI.create(BASE_TYPE + "/email-duplicado"));
        pd.setTitle("Email ya registrado");
        pd.setDetail("El email ya está en uso");
        return pd;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setType(URI.create(BASE_TYPE + "/credenciales-invalidas"));
        pd.setTitle("Credenciales inválidas");
        pd.setDetail("Email o contraseña incorrectos");
        return pd;
    }

    @ExceptionHandler(UserInactiveException.class)
    public ProblemDetail handleUserInactive(UserInactiveException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setType(URI.create(BASE_TYPE + "/usuario-inactivo"));
        pd.setTitle("Cuenta desactivada");
        pd.setDetail("Tu cuenta está desactivada");
        return pd;
    }

    @ExceptionHandler({TokenNotFoundException.class, TokenRevokedException.class, TokenExpiredException.class})
    public ProblemDetail handleTokenError(RuntimeException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setType(URI.create(BASE_TYPE + "/unauthorized"));
        pd.setTitle("No autorizado");
        pd.setDetail("Token inválido o expirado");
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setType(URI.create(BASE_TYPE + "/validacion"));
        pd.setTitle("Datos inválidos");
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation error");
        pd.setDetail(detail);
        return pd;
    }
}
