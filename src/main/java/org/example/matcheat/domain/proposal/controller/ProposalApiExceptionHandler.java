package org.example.matcheat.domain.proposal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Proposal API에서 발생하는 주요 예외를 HTTP 응답으로 변환한다.
 */
@RestControllerAdvice(
        basePackages = "org.example.matcheat.domain.proposal.controller"
)
public class ProposalApiExceptionHandler {

    /**
     * 판매자 자격이나 주문 소유권이 없는 요청을 처리한다.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(
            AccessDeniedException exception
    ) {
        return error(
                HttpStatus.FORBIDDEN,
                exception.getMessage()
        );
    }

    /**
     * 존재하지 않는 주문이나 상품 요청을 처리한다.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            IllegalArgumentException exception
    ) {
        return error(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    /**
     * 현재 상태에서 수행할 수 없는 제안 요청을 처리한다.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(
            IllegalStateException exception
    ) {
        return error(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    /**
     * Proposal 입력값 검증 실패를 처리한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        fieldErrors.putIfAbsent(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "message",
                "제안 입력값을 확인해주세요."
        );
        body.put(
                "fieldErrors",
                fieldErrors
        );

        return ResponseEntity
                .badRequest()
                .body(body);
    }

    private ResponseEntity<Map<String, Object>> error(
            HttpStatus status,
            String message
    ) {
        return ResponseEntity
                .status(status)
                .body(Map.of(
                        "message",
                        message
                ));
    }
}