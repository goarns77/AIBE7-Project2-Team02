package org.example.matcheat.domain.account.controller;

import org.example.matcheat.domain.account.enums.UserRole;
import org.example.matcheat.domain.account.enums.UserStatus;
import org.example.matcheat.domain.account.service.AccountAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthRestControllerMvcTest {
    @Mock
    private AccountAuthService accountAuthService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthRestController controller = new AuthRestController(accountAuthService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new AccountApiExceptionHandler())
                .build();
    }

    @Test
    void returnsCreatedSignupResponseWithoutPasswordFields() throws Exception {
        when(accountAuthService.signUp(any(), any(), any(), any())).thenReturn(new AccountAuthService.SignUpResult(
                1L, "user@example.com", "홍길동", UserRole.USER, UserStatus.ACTIVE));

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password1234",
                                  "passwordConfirm": "password1234",
                                  "name": "홍길동"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void returnsFieldErrorsForInvalidSignupRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "bad",
                                  "password": "short",
                                  "passwordConfirm": "short",
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void returnsLoginContract() throws Exception {
        when(accountAuthService.login(any(), any())).thenReturn(new AccountAuthService.LoginResult(
                "token", "Bearer", 3600,
                new AccountAuthService.UserSummary(1L, "user@example.com", "홍길동", UserRole.USER)));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"password1234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.user.role").value("USER"));
    }

    @Test
    void returnsEmailAvailabilityContract() throws Exception {
        when(accountAuthService.checkEmailAvailability("user@example.com"))
                .thenReturn(new AccountAuthService.EmailAvailability("user@example.com", true));

        mockMvc.perform(get("/api/v1/auth/email-availability").param("email", "user@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void returnsCommonErrorForMissingEmailParameter() throws Exception {
        mockMvc.perform(get("/api/v1/auth/email-availability"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }
}
