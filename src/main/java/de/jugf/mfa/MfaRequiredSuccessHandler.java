package de.jugf.mfa;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class MfaRequiredSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final Map<String, String> otpStore = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
            Authentication authentication) {

        String username = authentication.getName();

        // ✅ Generate simple OTP (replace with TOTP in production)
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));

        otpStore.put(username, otp);

        System.out.println("OTP for " + username + ": " + otp);

        ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
        response.setStatusCode(HttpStatus.ACCEPTED); // MFA required

        byte[] bytes = ("MFA_REQUIRED").getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    public boolean validateOtp(String username, String otp) {
        return otp.equals(otpStore.get(username));
    }
}
