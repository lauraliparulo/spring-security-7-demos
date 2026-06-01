package de.jugf.mfa;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class MfaSuccessHandler implements ServerAuthenticationSuccessHandler {

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange exchange,
                                              Authentication authentication) {

        ServerHttpResponse response = exchange.getExchange().getResponse();

        response.setStatusCode(HttpStatus.OK);

        String body = "{ \"status\": \"MFA_SUCCESS\" }";

        // return JWT token instead of simple message in production
        // String token = jwtService.generateToken(authentication);
        // String body = "{ \"token\": \"" + token + "\" }";

        return response.writeWith(
                Mono.just(response.bufferFactory()
                        .wrap(body.getBytes(StandardCharsets.UTF_8)))
        );
    }
}

