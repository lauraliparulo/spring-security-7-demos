package de.jugf.mfa;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.warrenstrange.googleauth.GoogleAuthenticator;

import reactor.core.publisher.Mono;

@Component
public class MfaRequiredSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final Map<String, String> userSecrets = new ConcurrentHashMap<>();
    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
            Authentication authentication) {

        // ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
        // response.setStatusCode(HttpStatus.ACCEPTED); // MFA required

        // byte[] bytes = ("MFA_REQUIRED").getBytes(StandardCharsets.UTF_8);
        // return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));


        
        ServerHttpResponse response = webFilterExchange.getExchange().getResponse();

        // ✅ Redirect to 2FA page
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().setLocation(URI.create("/setup-2fa"));

        return response.setComplete();


    }

    public boolean validateOtp(String username, String otp) {
        String secret = userSecrets.get(username);
        if (secret == null) {
            return false;
        }

        try {
            int otpCode = Integer.parseInt(otp);
            return googleAuthenticator.authorize(secret, otpCode);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void storeSecret(String username, String secret) {
        userSecrets.put(username, secret);
    }

    public boolean hasSecret(String username) {
        return userSecrets.containsKey(username);
    }
}
