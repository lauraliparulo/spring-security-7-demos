package de.jugf.mfa;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import de.jugf.redis.RedisOtpService;
import reactor.core.publisher.Mono;

@Component
public class LoginSuccessHandler implements ServerAuthenticationSuccessHandler {


    private final RedisOtpService otpService;

    public LoginSuccessHandler(RedisOtpService otpService) {
        this.otpService = otpService;
    }



    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange exchange,
                                              Authentication authentication) {

        String username = authentication.getName();

        String otp = String.valueOf(ThreadLocalRandom.current()
                .nextInt(100000, 999999));

        // ✅ Store in Redis
        return otpService.storeOtp(username, otp)
            .then(sendResponse(exchange, otp)); // (log only for demo)
    }

    private Mono<Void> sendResponse(WebFilterExchange exchange, String otp) {
        ServerHttpResponse response = exchange.getExchange().getResponse();

        response.setStatusCode(HttpStatus.ACCEPTED);

        String body = """
        {
          "status": "MFA_REQUIRED"
        }
        """;

        // ⚠️ don't expose OTP in real systems
        System.out.println("OTP: " + otp);

        return response.writeWith(
                Mono.just(response.bufferFactory()
                        .wrap(body.getBytes(StandardCharsets.UTF_8)))
        );
    }

    public Mono<Boolean> verifyOtp(String username, String otp) {
        return otpService.validateOtp(username, otp);
    }

}


