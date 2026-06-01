
package de.jugf.mfa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import reactor.test.StepVerifier;

class MfaAuthenticationConverterTest {

    private final MfaAuthenticationConverter converter = new MfaAuthenticationConverter();

    @Test
    void testConvertValidMfaRequest() {
        String jsonBody = "{\"username\":\"user\",\"otp\":\"123456\"}";
        
        MockServerHttpRequest request = MockServerHttpRequest
            .post("/mfa")
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonBody);
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        StepVerifier.create(converter.convert(exchange))
            .assertNext(auth -> {
                assertNotNull(auth);
                assertEquals("user", auth.getName());
                assertEquals("123456", auth.getCredentials());
                assertIsInstance(auth, UsernamePasswordAuthenticationToken.class);
            })
            .verifyComplete();
    }

    @Test
    void testConvertWithDifferentOtpLength() {
        String jsonBody = "{\"username\":\"testuser\",\"otp\":\"1234567890\"}";
        
        MockServerHttpRequest request = MockServerHttpRequest
            .post("/mfa")
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonBody);
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        StepVerifier.create(converter.convert(exchange))
            .assertNext(auth -> {
                assertEquals("testuser", auth.getName());
                assertEquals("1234567890", auth.getCredentials());
            })
            .verifyComplete();
    }

    @Test
    void testConvertWithLeadingZeroOtp() {
        String jsonBody = "{\"username\":\"user\",\"otp\":\"000001\"}";
        
        MockServerHttpRequest request = MockServerHttpRequest
            .post("/mfa")
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonBody);
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        StepVerifier.create(converter.convert(exchange))
            .assertNext(auth -> {
                assertEquals("user", auth.getName());
                assertEquals("000001", auth.getCredentials());
            })
            .verifyComplete();
    }

    @Test
    void testConvertPreservesUsername() {
        String jsonBody = "{\"username\":\"admin@example.com\",\"otp\":\"654321\"}";
        
        MockServerHttpRequest request = MockServerHttpRequest
            .post("/mfa")
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonBody);
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        StepVerifier.create(converter.convert(exchange))
            .assertNext(auth -> {
                assertEquals("admin@example.com", auth.getName());
            })
            .verifyComplete();
    }

    private void assertIsInstance(Object obj, Class<?> clazz) {
        assertTrue(clazz.isInstance(obj), 
            "Expected instance of " + clazz.getName() + " but got " + obj.getClass().getName());
    }
}
