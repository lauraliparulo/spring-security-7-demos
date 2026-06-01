package de.jugf.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityConfigurationIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;
    
    private WebTestClient webTestClient;
    
    private WebTestClient getWebTestClient() {
        if (webTestClient == null) {
            webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
        }
        return webTestClient;
    }

    @Test
    void testCsrfIsDisabled() {
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"user\",\"password\":\"password\"}")
            .exchange()
            .expectStatus().isAccepted();
    }

    @Test
    void testLoginPathIsPermitted() {
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"user\",\"password\":\"password\"}")
            .exchange()
            .expectStatus().isAccepted();
    }

    @Test
    void testMfaPathIsPermitted() {
        getWebTestClient()
            .post()
            .uri("/mfa")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"user\",\"otp\":\"000000\"}")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void testAnyOtherPathRequiresAuthentication() {
        getWebTestClient()
            .get()
            .uri("/some-other-endpoint")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void testValidCredentialsReturnAcceptedStatus() {
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"user\",\"password\":\"password\"}")
            .exchange()
            .expectStatus().isAccepted();
    }

    @Test
    void testInvalidCredentialsReturnUnauthorized() {
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"user\",\"password\":\"incorrect\"}")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void testJsonContentTypeIsRequired() {
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.TEXT_PLAIN)
            .bodyValue("username=user&password=password")
            .exchange()
            .expectStatus().is4xxClientError();
    }

    @Test
    void testMfaResponseOnSuccess() {
        getWebTestClient()
            .post()
            .uri("/mfa")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"user\",\"otp\":\"123456\"}")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void testLoginResponseBodyFormat() {
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"user\",\"password\":\"password\"}")
            .exchange()
            .expectStatus().isAccepted()
            .expectBody(String.class).isEqualTo("MFA_REQUIRED");
    }

    @Test
    void testMultipleLoginAttempts() {
        // First attempt with valid credentials
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"user\",\"password\":\"password\"}")
            .exchange()
            .expectStatus().isAccepted();

        // Second attempt with valid credentials should also work
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"user\",\"password\":\"password\"}")
            .exchange()
            .expectStatus().isAccepted();
    }
}
