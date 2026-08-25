package org.example.matcheat.account.api.auth;

import jakarta.validation.Valid;
import org.example.matcheat.account.port.in.CheckEmailAvailabilityUseCase;
import org.example.matcheat.account.port.in.LoginUseCase;
import org.example.matcheat.account.port.in.SignUpUseCase;
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
    private final SignUpUseCase signUpUseCase;
    private final LoginUseCase loginUseCase;
    private final CheckEmailAvailabilityUseCase emailAvailabilityUseCase;

    public AuthRestController(
            SignUpUseCase signUpUseCase,
            LoginUseCase loginUseCase,
            CheckEmailAvailabilityUseCase emailAvailabilityUseCase) {
        this.signUpUseCase = signUpUseCase;
        this.loginUseCase = loginUseCase;
        this.emailAvailabilityUseCase = emailAvailabilityUseCase;
    }

    @GetMapping("/email-availability")
    public EmailAvailabilityResponse checkEmail(@RequestParam("email") String email) {
        return EmailAvailabilityResponse.from(emailAvailabilityUseCase.check(email));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpUseCase.SignUpResult result = signUpUseCase.signUp(new SignUpUseCase.SignUpCommand(
                request.email(), request.password(), request.passwordConfirm(), request.name()));
        return ResponseEntity.status(HttpStatus.CREATED).body(SignUpResponse.from(result));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = LoginResponse.from(loginUseCase.login(new LoginUseCase.LoginCommand(
                request.email(), request.password())));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(response);
    }
}
