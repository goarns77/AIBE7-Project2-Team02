package org.example.matcheat.domain.account.controller;

import org.example.matcheat.config.SecurityConfig;
import org.example.matcheat.domain.account.dto.AccountReportResponse;
import org.example.matcheat.domain.account.enums.AccountReportStatus;
import org.example.matcheat.domain.account.security.AccountSecurityErrorHandler;
import org.example.matcheat.domain.account.service.AccountReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AccountReportRestController.class)
@Import({SecurityConfig.class, AccountApiExceptionHandler.class, AccountSecurityErrorHandler.class,
        AccountReportRestControllerMvcTest.SecurityTestBeans.class})
class AccountReportRestControllerMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountReportService service;

    @Test
    void rejectsUnauthenticatedReport() throws Exception {
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"신고\",\"message\":\"내용\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createsReportUsingJwtSubject() throws Exception {
        when(service.create(org.mockito.ArgumentMatchers.eq(7L), any())).thenReturn(new AccountReportResponse(
                3L, "신고", "확인해 주세요.", AccountReportStatus.PENDING, null,
                Instant.parse("2026-09-02T03:00:00Z"), Instant.parse("2026-09-02T03:00:00Z"), null));

        mockMvc.perform(post("/api/v1/reports")
                        .with(jwt().jwt(token -> token.subject("7")).authorities(() -> "ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"신고\",\"message\":\"확인해 주세요.\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportId").value(3))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(service).create(org.mockito.ArgumentMatchers.eq(7L), any());
    }

    @Test
    void validatesReportBody() throws Exception {
        mockMvc.perform(post("/api/v1/reports")
                        .with(jwt().jwt(token -> token.subject("7")).authorities(() -> "ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"message\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.message").exists());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class SecurityTestBeans {
        @Bean("accountJwtDecoder")
        JwtDecoder accountJwtDecoder() {
            return mock(JwtDecoder.class);
        }

        @Bean("accountJwtAuthenticationConverter")
        Converter<Jwt, AbstractAuthenticationToken> accountJwtAuthenticationConverter() {
            return JwtAuthenticationToken::new;
        }
    }
}
