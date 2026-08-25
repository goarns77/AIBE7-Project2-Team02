package org.example.matcheat.account.api;

import jakarta.servlet.http.HttpServletRequest;
import org.example.matcheat.account.application.AccountApplicationException;
import org.example.matcheat.account.application.AccountErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "org.example.matcheat.account.api")
public class AccountApiExceptionHandler {
    @ExceptionHandler(AccountApplicationException.class)
    ResponseEntity<ApiErrorResponse> handleAccountError(
            AccountApplicationException exception,
            HttpServletRequest request) {
        HttpStatus status = statusOf(exception.code());
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                exception.code().name(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                AccountErrorCode.VALIDATION_FAILED.name(),
                "입력값을 확인해 주세요.",
                request.getRequestURI(),
                fieldErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpServletRequest request) {
        return badRequest(
                AccountErrorCode.VALIDATION_FAILED,
                "요청 본문을 확인해 주세요.",
                request,
                Map.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ApiErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request) {
        Map<String, String> fields = Map.of(exception.getParameterName(), "필수 값입니다.");
        return badRequest(AccountErrorCode.VALIDATION_FAILED, "입력값을 확인해 주세요.", request, fields);
    }

    private static ResponseEntity<ApiErrorResponse> badRequest(
            AccountErrorCode code,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(), status.value(), code.name(), message, request.getRequestURI(), fieldErrors));
    }

    private static HttpStatus statusOf(AccountErrorCode code) {
        return switch (code) {
            case EMAIL_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case ACCOUNT_SUSPENDED, ACCOUNT_WITHDRAWN -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
