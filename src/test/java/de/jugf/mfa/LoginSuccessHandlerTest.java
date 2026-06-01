package de.jugf.mfa;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.WebFilterExchange;

import de.jugf.redis.RedisOtpService;

import java.util.List;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class LoginSuccessHandlerTest {

    private LoginSuccessHandler handler;
    
    @Mock
    private RedisOtpService otpService;
    
    private MockServerHttpRequest request;
    private MockServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new LoginSuccessHandler(otpService);
        request = MockServerHttpRequest.post("/login").build();
        exchange = MockServerWebExchange.from(request);
    }

    @Test
    void testOnAuthenticationSuccessCallsStoreOtp() {
        String username = "user";
        Authentication auth = new UsernamePasswordAuthenticationToken(
            username, "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        when(otpService.storeOtp(anyString(), anyString())).thenReturn(Mono.empty());
        
        WebFilterExchange webFilterExchange = new WebFilterExchange(exchange, (ex) -> null);
        
        StepVerifier.create(handler.onAuthenticationSuccess(webFilterExchange, auth))
            .verifyComplete();
        
        verify(otpService).storeOtp(eq(username), anyString());
    }

    @Test
    void testOnAuthenticationSuccessResponseStatusIsAccepted() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "user", "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        when(otpService.storeOtp(anyString(), anyString())).thenReturn(Mono.empty());
        
        WebFilterExchange webFilterExchange = new WebFilterExchange(exchange, (ex) -> null);
        
        StepVerifier.create(handler.onAuthenticationSuccess(webFilterExchange, auth))
            .verifyComplete();
        
        assertEquals(HttpStatus.ACCEPTED, exchange.getResponse().getStatusCode());
    }

    @Test
    void testVerifyOtpDelegatesToOtpService() {
        String username = "user";
        String otp = "123456";
        
        when(otpService.validateOtp(username, otp)).thenReturn(Mono.just(true));
        
        StepVerifier.create(handler.verifyOtp(username, otp))
            .expectNext(true)
            .verifyComplete();
        
        verify(otpService).validateOtp(username, otp);
    }

    @Test
    void testVerifyOtpReturnsFalseForInvalidOtp() {
        String username = "user";
        String otp = "654321";
        
        when(otpService.validateOtp(username, otp)).thenReturn(Mono.just(false));
        
        StepVerifier.create(handler.verifyOtp(username, otp))
            .expectNext(false)
            .verifyComplete();
    }
}
