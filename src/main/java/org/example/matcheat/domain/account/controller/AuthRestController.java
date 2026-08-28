package org.example.matcheat.domain.account.controller;

import jakarta.validation.Valid;
import org.example.matcheat.domain.account.dto.EmailAvailabilityResponse;
import org.example.matcheat.domain.account.dto.LoginRequest;
import org.example.matcheat.domain.account.dto.LoginResponse;
import org.example.matcheat.domain.account.dto.SignUpRequest;
import org.example.matcheat.domain.account.dto.SignUpResponse;
import org.example.matcheat.domain.account.service.AccountAuthService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthRestController {
    private final AccountAuthService accountAuthService;

    public AuthRestController(AccountAuthService accountAuthService) {
        this.accountAuthService = accountAuthService;
    }

    @GetMapping("/email-availability")
    public EmailAvailabilityResponse checkEmail(@RequestParam("email") String email) {
        return EmailAvailabilityResponse.from(accountAuthService.checkEmailAvailability(email));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        AccountAuthService.SignUpResult result = accountAuthService.signUp(
                request.email(), request.password(), request.passwordConfirm(), request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(SignUpResponse.from(result));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = LoginResponse.from(accountAuthService.login(request.email(), request.password()));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(response);
    }
}
