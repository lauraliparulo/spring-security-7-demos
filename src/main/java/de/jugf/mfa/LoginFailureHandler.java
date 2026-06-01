package de.jugf.mfa;

import java.net.URI;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;



@Component
public class LoginFailureHandler       implements ServerAuthenticationFailureHandler {


    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange exchange,
                                              AuthenticationException ex) {

        ServerHttpResponse response = exchange.getExchange().getResponse();

        // ✅ Custom header
        response.getHeaders().add("failed", LocalDateTime.now().toString());

        // ✅ Redirect to error page
        response.setStatusCode(HttpStatus.FOUND); // 302
        response.getHeaders().setLocation(URI.create("/loginerror"));

        return response.setComplete();
    }


}
