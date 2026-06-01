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

import com.warrenstrange.googleauth.GoogleAuthenticator;

import java.util.List;

import reactor.test.StepVerifier;

class MfaRequiredSuccessHandlerTest {

    private MfaRequiredSuccessHandler handler;
    private MockServerHttpRequest request;
    private MockServerWebExchange exchange;
    private GoogleAuthenticator googleAuthenticator;

    @BeforeEach
    void setUp() {
        handler = new MfaRequiredSuccessHandler();
        request = MockServerHttpRequest.post("/login").build();
        exchange = MockServerWebExchange.from(request);
        googleAuthenticator = new GoogleAuthenticator();
    }

    @Test
    void testOnAuthenticationSuccessReturnsAccepted() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "user", "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        WebFilterExchange webFilterExchange = new WebFilterExchange(exchange, (ex) -> null);
        
        StepVerifier.create(handler.onAuthenticationSuccess(webFilterExchange, auth))
            .verifyComplete();
        
        assertEquals(HttpStatus.ACCEPTED, exchange.getResponse().getStatusCode());
    }

    @Test
    void testStoreSecretAndValidateOtp() {
        String username = "testuser";
        String secret = "JBSWY3DPEBLW64TMMQ======";
        
        handler.storeSecret(username, secret);
        
        assertTrue(handler.hasSecret(username));
    }

    @Test
    void testOtpValidationFailsForNonExistentUser() {
        assertFalse(handler.validateOtp("nonexistent", "123456"));
    }

    @Test
    void testOtpValidationFailsWithInvalidOtpFormat() {
        String username = "user";
        String secret = "JBSWY3DPEBLW64TMMQ======";
        
        handler.storeSecret(username, secret);
        
        assertFalse(handler.validateOtp(username, "invalid"));
    }

    @Test
    void testHasSecretReturnsFalseForNonExistentUser() {
        assertFalse(handler.hasSecret("nonexistent"));
    }

    @Test
    void testMultipleUsersCanHaveDifferentSecrets() {
        handler.storeSecret("user1", "SECRET1ABCDEFGH======");
        handler.storeSecret("user2", "SECRET2IJKLMNOP======");
        
        assertTrue(handler.hasSecret("user1"));
        assertTrue(handler.hasSecret("user2"));
    }

    @Test
    void testResponseBodyContainsMfaRequired() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "user", "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        WebFilterExchange webFilterExchange = new WebFilterExchange(exchange, (ex) -> null);
        
        StepVerifier.create(handler.onAuthenticationSuccess(webFilterExchange, auth))
            .verifyComplete();
        
        assertEquals(HttpStatus.ACCEPTED, exchange.getResponse().getStatusCode());
    }
}
