package de.jugf.redis;  

import java.time.Duration;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;


@Component
public class RedisOtpService  {

    private final ReactiveStringRedisTemplate redis;

    public RedisOtpService(ReactiveStringRedisTemplate redis) {
        this.redis = redis;
    }


    private String key(String username) {
        return "otp:" + username;
    }

    // ✅ Store OTP with TTL
    public Mono<Void> storeOtp(String username, String otp) {
        return redis.opsForValue()
                .set(key(username), otp, Duration.ofSeconds(30))
                .then();
    }

    // ✅ Validate + invalidate OTP
    public Mono<Boolean> validateOtp(String username, String otp) {
        String key = key(username);

        return redis.opsForValue().get(key)
            .flatMap(stored -> {
                if (otp.equals(stored)) {
                    // ✅ delete after success (one-time use)
                    return redis.delete(key).thenReturn(true);
                }
                return Mono.just(false);
            })
            .defaultIfEmpty(false);
    }
}