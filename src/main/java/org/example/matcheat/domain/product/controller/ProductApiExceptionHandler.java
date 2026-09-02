package org.example.matcheat.domain.product.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 판매 조건(Product) API와 판매자 추천(Matching/Product) API에서 발생하는 주요 예외를
 * 사람이 읽을 수 있는 HTTP 응답으로 변환한다. ProposalApiExceptionHandler와 같은 패턴이다.
 */
@RestControllerAdvice(basePackages = {
        "org.example.matcheat.domain.product.controller",
        "org.example.matcheat.domain.matching.product.controller"
})
public class ProductApiExceptionHandler {

    /**
     * 소유자가 아니거나 관리자가 아닌 요청(수정/삭제/추천 조회 등)을 처리한다.
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
     * 존재하지 않는 판매 조건 ID, 승인되지 않은 판매자 등 잘못된 요청값을 처리한다.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            IllegalArgumentException exception
    ) {
        String message = exception.getMessage();
        HttpStatus status = message != null
                && (message.contains("존재하지") || message.contains("찾을 수 없"))
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;

        return error(status, message);
    }

    /**
     * 현재 상태에서 수행할 수 없는 요청(예: 이미 처리된 상태 등)을 처리한다.
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
     * 판매 조건 입력값(@Valid) 검증 실패를 처리한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(fieldError ->
                        fieldErrors.putIfAbsent(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        )
                );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "판매 조건 입력값을 확인해주세요.");
        body.put("fieldErrors", fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(Map.of("message", message));
    }
}
