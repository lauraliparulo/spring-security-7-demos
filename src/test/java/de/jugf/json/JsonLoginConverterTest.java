package de.jugf.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import reactor.test.StepVerifier;

class JsonLoginConverterTest {

    private final JsonLoginConverter converter = new JsonLoginConverter();

    @Test
    void testConvertValidLoginRequest() {
        String jsonBody = "{\"username\":\"user\",\"password\":\"password\"}";
        
        MockServerHttpRequest request = MockServerHttpRequest
            .post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonBody);
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        StepVerifier.create(converter.convert(exchange))
            .assertNext(auth -> {
                assertNotNull(auth);
                assertEquals("user", auth.getName());
                assertEquals("password", auth.getCredentials());
                assertIsInstance(auth, UsernamePasswordAuthenticationToken.class);
            })
            .verifyComplete();
    }

    @Test
    void testConvertWithSpecialCharacters() {
        String jsonBody = "{\"username\":\"user@example.com\",\"password\":\"p@ssw0rd!\"}";
        
        MockServerHttpRequest request = MockServerHttpRequest
            .post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonBody);
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        StepVerifier.create(converter.convert(exchange))
            .assertNext(auth -> {
                assertEquals("user@example.com", auth.getName());
                assertEquals("p@ssw0rd!", auth.getCredentials());
            })
            .verifyComplete();
    }

    @Test
    void testConvertWithEmptyCredentials() {
        String jsonBody = "{\"username\":\"user\",\"password\":\"\"}";
        
        MockServerHttpRequest request = MockServerHttpRequest
            .post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonBody);
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        StepVerifier.create(converter.convert(exchange))
            .assertNext(auth -> {
                assertEquals("user", auth.getName());
                assertEquals("", auth.getCredentials());
            })
            .verifyComplete();
    }

    private void assertIsInstance(Object obj, Class<?> clazz) {
        assertTrue(clazz.isInstance(obj), 
            "Expected instance of " + clazz.getName() + " but got " + obj.getClass().getName());
    }
}
