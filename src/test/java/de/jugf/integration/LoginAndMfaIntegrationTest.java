package de.jugf.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoginAndMfaIntegrationTest {

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
    void testLoginWithValidCredentialsRedirectsTo2faSetup() {
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"admin\",\"password\":\"pass123\"}")
            .exchange()
            .expectStatus().isFound()
            .expectHeader().location("/setup-2fa");
    }

    @Test
    void testLoginWithInvalidPassword() {
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"admin\",\"password\":\"wrongpassword\"}")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void testLoginWithInvalidUsername() {
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"invaliduser\",\"password\":\"pass123\"}")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void testSetup2faPageRequiresAuthentication() {
        getWebTestClient()
            .get()
            .uri("/setup-2fa")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void testMfaVerifyEndpointRequiresAuthentication() {
        getWebTestClient()
            .post()
            .uri("/mfa/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"otp\":\"123456\"}")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void testLoginJsonFormatWithTrimmedSpaces() {
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{  \"username\"  :  \"admin\"  ,  \"password\"  :  \"pass123\"  }")
            .exchange()
            .expectStatus().isFound()
            .expectHeader().location("/setup-2fa");
    }

    @Test
    void testLoginWithEmptyUsername() {
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"\",\"password\":\"pass123\"}")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void testLoginContentTypeJson() {
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"admin\",\"password\":\"pass123\"}")
            .exchange()
            .expectStatus().isFound();
    }

    @Test
    void testUnauthenticatedAccessToDeniedEndpoint() {
        getWebTestClient()
            .get()
            .uri("/protected-resource")
            .exchange()
            .expectStatus().isUnauthorized();
    }
}
