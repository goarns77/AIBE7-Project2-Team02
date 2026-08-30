package org.example.matcheat.domain.account.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.matcheat.domain.account.dto.ApiErrorResponse;
import org.example.matcheat.domain.account.service.AccountErrorCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
public class AccountSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    public AccountSecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        writeError(
                request,
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                AccountErrorCode.INVALID_TOKEN,
                "인증이 필요하거나 토큰이 유효하지 않습니다.");
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.access.AccessDeniedException exception) throws IOException {
        writeError(
                request,
                response,
                HttpServletResponse.SC_FORBIDDEN,
                AccountErrorCode.FORBIDDEN,
                "요청한 작업을 수행할 권한이 없습니다.");
    }

    private void writeError(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            AccountErrorCode code,
            String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ApiErrorResponse(
                Instant.now(),
                status,
                code.name(),
                message,
                request.getRequestURI(),
                Map.of()));
    }
}
