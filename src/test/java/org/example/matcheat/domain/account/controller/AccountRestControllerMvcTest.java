package org.example.matcheat.domain.account.controller;

import org.example.matcheat.config.SecurityConfig;
import org.example.matcheat.domain.account.enums.SellerVerificationStatus;
import org.example.matcheat.domain.account.enums.UserRole;
import org.example.matcheat.domain.account.enums.UserStatus;
import org.example.matcheat.domain.account.security.AccountSecurityErrorHandler;
import org.example.matcheat.domain.account.service.AccountProfileService;
import org.example.matcheat.domain.account.service.AccountApplicationException;
import org.example.matcheat.domain.account.service.AccountErrorCode;
import org.example.matcheat.domain.account.service.SellerApplicationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AccountRestController.class)
@Import({
        SecurityConfig.class,
        AccountApiExceptionHandler.class,
        AccountSecurityErrorHandler.class,
        AccountRestControllerMvcTest.SecurityTestBeans.class
})
class AccountRestControllerMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountProfileService accountProfileService;

    @MockitoBean
    private SellerApplicationService sellerApplicationService;

    @Test
    void rejectsUnauthenticatedProfileRequest() throws Exception {
        mockMvc.perform(get("/api/v1/account/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void readsCurrentProfileUsingJwtSubject() throws Exception {
        when(accountProfileService.getCurrentUser(7L)).thenReturn(profile("홍길동"));

        mockMvc.perform(get("/api/v1/account/me").with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.sellerStatus").value("PENDING"));

        verify(accountProfileService).getCurrentUser(7L);
    }

    @Test
    void updatesNameUsingJwtSubject() throws Exception {
        when(accountProfileService.updateName(7L, "새 이름")).thenReturn(profile("새 이름"));

        mockMvc.perform(patch("/api/v1/account/me")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"새 이름\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("새 이름"));

        verify(accountProfileService).updateName(7L, "새 이름");
    }

    @Test
    void withdrawsUsingJwtSubjectAndCurrentPassword() throws Exception {
        mockMvc.perform(delete("/api/v1/account/me")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"password1234\"}"))
                .andExpect(status().isNoContent());

        verify(accountProfileService).withdraw(7L, "password1234");
    }

    @Test
    void returnsConflictWhenWithdrawalWouldInterruptTrade() throws Exception {
        doThrow(new AccountApplicationException(
                AccountErrorCode.ACTIVE_TRANSACTION_EXISTS,
                "진행 중인 거래가 있어 회원 탈퇴를 할 수 없습니다."))
                .when(accountProfileService).withdraw(7L, "password1234");

        mockMvc.perform(delete("/api/v1/account/me")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"password1234\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ACTIVE_TRANSACTION_EXISTS"));
    }

    @Test
    void createsSellerApplicationUsingJwtSubject() throws Exception {
        when(sellerApplicationService.apply(anyLong(), any(), any(), any(), any(), any()))
                .thenReturn(new SellerApplicationService.ApplicationResult(3L, SellerVerificationStatus.PENDING));

        mockMvc.perform(post("/api/v1/account/seller-applications")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessName":"매치잇 상회",
                                  "businessNumber":"123-45-67890",
                                  "latitude":37.5665,
                                  "longitude":126.9780,
                                  "deliveryRadiusKm":10.0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sellerId").value(3))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(sellerApplicationService).apply(
                7L, "매치잇 상회", "123-45-67890",
                new java.math.BigDecimal("37.5665"),
                new java.math.BigDecimal("126.9780"),
                new java.math.BigDecimal("10.0"));
    }

    @Test
    void returnsFieldErrorsForInvalidAccountRequests() throws Exception {
        mockMvc.perform(patch("/api/v1/account/me")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());

        mockMvc.perform(post("/api/v1/account/seller-applications")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.businessName").exists())
                .andExpect(jsonPath("$.fieldErrors.businessNumber").exists());
    }

    private static AccountProfileService.ProfileResult profile(String name) {
        return new AccountProfileService.ProfileResult(
                7L,
                "user@example.com",
                name,
                UserRole.USER,
                UserStatus.ACTIVE,
                SellerVerificationStatus.PENDING);
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor userJwt() {
        return jwt().jwt(token -> token.subject("7").claim("role", "USER"))
                .authorities(() -> "ROLE_USER");
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
