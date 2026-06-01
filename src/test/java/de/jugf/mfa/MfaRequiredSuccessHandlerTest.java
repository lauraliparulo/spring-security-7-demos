package de.jugf.mfa;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import reactor.test.StepVerifier;

class MfaRequiredSuccessHandlerTest {

    private MfaRequiredSuccessHandler handler;
    private MockServerHttpRequest request;
    private MockServerWebExchange exchange;
    private Map<String, String> otpStore;

    @BeforeEach
    void setUp() {
        handler = new MfaRequiredSuccessHandler();
        request = MockServerHttpRequest.post("/login").build();
        exchange = MockServerWebExchange.from(request);
        otpStore = getOtpStore();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getOtpStore() {
        return (Map<String, String>) ReflectionTestUtils.getField(handler, "otpStore");
    }

    @Test
    void testOnAuthenticationSuccessGeneratesOtp() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "user", "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        WebFilterExchange webFilterExchange = new WebFilterExchange(exchange, (ex) -> null);
        
        StepVerifier.create(handler.onAuthenticationSuccess(webFilterExchange, auth))
            .verifyComplete();
        
        String otp = otpStore.get("user");
        assertTrue(handler.validateOtp("user", otp));
    }

    @Test
    void testOtpIsGeneratedForUser() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "testuser", "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        WebFilterExchange webFilterExchange = new WebFilterExchange(exchange, (ex) -> null);
        
        StepVerifier.create(handler.onAuthenticationSuccess(webFilterExchange, auth))
            .verifyComplete();
        
        String storedOtp = otpStore.get("testuser");
        assertNotNull(storedOtp);
        assertFalse(storedOtp.isEmpty());
        assertTrue(storedOtp.matches("\\d{6}"));
    }

    @Test
    void testOtpValidationSucceedsWithCorrectOtp() {
        String username = "user";
        String otp = "123456";
        
        otpStore.put(username, otp);
        
        assertTrue(handler.validateOtp(username, otp));
    }

    @Test
    void testOtpValidationFailsWithIncorrectOtp() {
        String username = "user";
        otpStore.put(username, "123456");
        
        assertFalse(handler.validateOtp(username, "654321"));
    }

    @Test
    void testOtpValidationFailsForNonExistentUser() {
        assertFalse(handler.validateOtp("nonexistent", "123456"));
    }

    @Test
    void testMultipleUsersHaveDifferentOtps() {
        Authentication auth1 = new UsernamePasswordAuthenticationToken(
            "user1", "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Authentication auth2 = new UsernamePasswordAuthenticationToken(
            "user2", "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        MockServerWebExchange exchange1 = MockServerWebExchange.from(MockServerHttpRequest.post("/login").build());
        MockServerWebExchange exchange2 = MockServerWebExchange.from(MockServerHttpRequest.post("/login").build());
        
        WebFilterExchange webFilterExchange1 = new WebFilterExchange(exchange1, (ex) -> null);
        WebFilterExchange webFilterExchange2 = new WebFilterExchange(exchange2, (ex) -> null);
        
        StepVerifier.create(handler.onAuthenticationSuccess(webFilterExchange1, auth1))
            .verifyComplete();
        
        StepVerifier.create(handler.onAuthenticationSuccess(webFilterExchange2, auth2))
            .verifyComplete();
        
        String otp1 = otpStore.get("user1");
        String otp2 = otpStore.get("user2");
        
        assertNotNull(otp1);
        assertNotNull(otp2);
        assertNotEquals(otp1, otp2);
    }

    @Test
    void testResponseStatusIsAccepted() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "user", "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        WebFilterExchange webFilterExchange = new WebFilterExchange(exchange, (ex) -> null);
        
        StepVerifier.create(handler.onAuthenticationSuccess(webFilterExchange, auth))
            .verifyComplete();
        
        assertEquals(HttpStatus.ACCEPTED, exchange.getResponse().getStatusCode());
    }

    @Test
    void testOtpOverwrittenForSameUser() {
        String username = "user";
        otpStore.put(username, "111111");
        
        Authentication auth = new UsernamePasswordAuthenticationToken(
            username, "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        WebFilterExchange webFilterExchange = new WebFilterExchange(exchange, (ex) -> null);
        
        StepVerifier.create(handler.onAuthenticationSuccess(webFilterExchange, auth))
            .verifyComplete();
        
        String newOtp = otpStore.get(username);
        assertNotEquals("111111", newOtp);
    }
}
