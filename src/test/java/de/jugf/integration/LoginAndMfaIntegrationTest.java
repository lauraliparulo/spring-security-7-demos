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
    void testLoginWithValidCredentials() {
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"user\",\"password\":\"password\"}")
            .exchange()
            .expectStatus().isAccepted()
            .expectBody(String.class)
            .isEqualTo("MFA_REQUIRED");
    }

    @Test
    void testLoginWithInvalidPassword() {
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"user\",\"password\":\"wrongpassword\"}")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void testLoginWithInvalidUsername() {
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"invaliduser\",\"password\":\"password\"}")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void testMfaEndpointWithInvalidOtp() {
        getWebTestClient()
            .post()
            .uri("/mfa")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"user\",\"otp\":\"123456\"}")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void testLoginJsonFormatWithTrimmedSpaces() {
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{  \"username\"  :  \"user\"  ,  \"password\"  :  \"password\"  }")
            .exchange()
            .expectStatus().isAccepted();
    }

    @Test
    void testLoginWithEmptyUsername() {
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"\",\"password\":\"password\"}")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void testLoginResponseBodyContainsMfaRequired() {
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
    void testMfaEndpointPermittedWithoutAuth() {
        getWebTestClient()
            .post()
            .uri("/mfa")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"user\",\"otp\":\"123456\"}")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void testLoginContentTypeJson() {
        getWebTestClient()
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"user\",\"password\":\"password\"}")
            .exchange()
            .expectStatus().isAccepted();
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
