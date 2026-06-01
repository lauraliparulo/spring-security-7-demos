package de.jugf.mfa;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class MfaController {

    private final LoginSuccessHandler loginSuccessHandler;

    public MfaController(LoginSuccessHandler loginSuccessHandler) {
        this.loginSuccessHandler = loginSuccessHandler;
    }

    @PostMapping("/mfa")
    public Mono<ResponseEntity<String>> verify(@RequestBody Map<String, String> request) {

        String username = request.get("username");
        String otp = request.get("otp");

        return loginSuccessHandler.verifyOtp(username, otp)
            .flatMap(isValid -> {
                if (isValid) {
                    return Mono.just(ResponseEntity.ok("MFA SUCCESS"));
                }
                return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("INVALID OTP"));
            });
    }
}
