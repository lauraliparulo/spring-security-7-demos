package de.jugf.mfa;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import de.jugf.mfa.google.GoogleAuthService;
import reactor.core.publisher.Mono;

@Component
public class MfaRequiredSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final Map<String, String> otpStore = new ConcurrentHashMap<>();

    @Autowired
    private GoogleAuthService googleAuthService;

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


        String secret = googleAuthService.generateSecret();

        // ✅ MUST return the reactive chain
        return webFilterExchange.getExchange().getSession()
            .doOnNext(session -> {
                     session.getAttributes().put("TOTP_SECRET", secret);
                session.getAttributes().put("MFA_REQUIRED", true);
                session.getAttributes().put("username", username);
            })
            .then(redirect(webFilterExchange, "/login-mfa")); // ✅ returned

        
    }

    public boolean validateOtp(String username, String otp) {
        return otp.equals(otpStore.get(username));
    }


    
    private Mono<Void> redirect(WebFilterExchange exchange, String url) {
        ServerHttpResponse response = exchange.getExchange().getResponse();
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(URI.create(url));
        return response.setComplete();
    }

}
