package de.jugf.redis;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
class RedisOtpServiceTest {

    @Autowired
    private ReactiveStringRedisTemplate reactiveRedisTemplate;

    @Autowired
    private RedisOtpService otpService;

    @BeforeEach
    void setUp() {
        // Clean up Redis before each test
        reactiveRedisTemplate.getConnectionFactory().getReactiveConnection()
            .serverCommands().flushAll().block();
    }

    @Test
    void testStoreOtpSuccessfully() {
        String username = "user@example.com";
        String otp = "123456";

        StepVerifier.create(otpService.storeOtp(username, otp))
            .verifyComplete();

        // Verify OTP was stored
        StepVerifier.create(reactiveRedisTemplate.opsForValue().get("otp:" + username))
            .expectNext(otp)
            .verifyComplete();
    }

    @Test
    void testValidateOtpWithCorrectOtp() {
        String username = "user@example.com";
        String otp = "654321";

        StepVerifier.create(otpService.storeOtp(username, otp))
            .verifyComplete();

        StepVerifier.create(otpService.validateOtp(username, otp))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    void testValidateOtpDeletesOtpAfterSuccessfulValidation() {
        String username = "user@example.com";
        String otp = "123456";

        StepVerifier.create(otpService.storeOtp(username, otp))
            .verifyComplete();

        StepVerifier.create(otpService.validateOtp(username, otp))
            .expectNext(true)
            .verifyComplete();

        // Verify OTP was deleted (one-time use)
        StepVerifier.create(reactiveRedisTemplate.opsForValue().get("otp:" + username))
            .verifyComplete();
    }

    @Test
    void testValidateOtpWithIncorrectOtp() {
        String username = "user@example.com";
        String storedOtp = "123456";
        String wrongOtp = "654321";

        StepVerifier.create(otpService.storeOtp(username, storedOtp))
            .verifyComplete();

        StepVerifier.create(otpService.validateOtp(username, wrongOtp))
            .expectNext(false)
            .verifyComplete();

        // Verify OTP was NOT deleted when validation failed
        StepVerifier.create(reactiveRedisTemplate.opsForValue().get("otp:" + username))
            .expectNext(storedOtp)
            .verifyComplete();
    }

    @Test
    void testValidateOtpForNonExistentUser() {
        String username = "nonexistent@example.com";
        String otp = "123456";

        StepVerifier.create(otpService.validateOtp(username, otp))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    void testMultipleUsersHaveSeparateOtps() {
        String user1 = "user1@example.com";
        String user2 = "user2@example.com";
        String otp1 = "111111";
        String otp2 = "222222";

        StepVerifier.create(otpService.storeOtp(user1, otp1))
            .verifyComplete();

        StepVerifier.create(otpService.storeOtp(user2, otp2))
            .verifyComplete();

        StepVerifier.create(otpService.validateOtp(user1, otp1))
            .expectNext(true)
            .verifyComplete();

        // User2's OTP should still be valid
        StepVerifier.create(otpService.validateOtp(user2, otp2))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    void testOtpCannotBeUsedTwice() {
        String username = "user@example.com";
        String otp = "123456";

        StepVerifier.create(otpService.storeOtp(username, otp))
            .verifyComplete();

        // First validation succeeds
        StepVerifier.create(otpService.validateOtp(username, otp))
            .expectNext(true)
            .verifyComplete();

        // Second validation fails (one-time use)
        StepVerifier.create(otpService.validateOtp(username, otp))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    void testOtpExpiresAfterTtl() throws InterruptedException {
        String username = "user@example.com";
        String otp = "123456";

        // Store OTP with very short TTL (100ms)
        reactiveRedisTemplate.opsForValue()
            .set("otp:" + username, otp, Duration.ofMillis(100))
            .subscribe();

        // Wait for expiration
        Thread.sleep(150);

        StepVerifier.create(otpService.validateOtp(username, otp))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    void testOverwriteOtpForSameUser() {
        String username = "user@example.com";
        String otp1 = "111111";
        String otp2 = "222222";

        StepVerifier.create(otpService.storeOtp(username, otp1))
            .verifyComplete();

        StepVerifier.create(otpService.storeOtp(username, otp2))
            .verifyComplete();

        // Old OTP should not work
        StepVerifier.create(otpService.validateOtp(username, otp1))
            .expectNext(false)
            .verifyComplete();

        // New OTP should work
        StepVerifier.create(otpService.validateOtp(username, otp2))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    void testEmptyUsernameHandling() {
        String username = "";
        String otp = "123456";

        StepVerifier.create(otpService.storeOtp(username, otp))
            .verifyComplete();

        StepVerifier.create(otpService.validateOtp(username, otp))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    void testSpecialCharactersInUsernameAndOtp() {
        String username = "user+test@example.com";
        String otp = "abc@123!";

        StepVerifier.create(otpService.storeOtp(username, otp))
            .verifyComplete();

        StepVerifier.create(otpService.validateOtp(username, otp))
            .expectNext(true)
            .verifyComplete();
    }
}
