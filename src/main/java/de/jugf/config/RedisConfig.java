package de.jugf.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import jakarta.annotation.PreDestroy;
import redis.embedded.RedisServer;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    private RedisServer redisServer;

    @Bean
    public RedisServer embeddedRedisServer(RedisProperties redisProperties) throws Exception {
        redisServer = new RedisServer(redisProperties.getRedisPort());
        redisServer.start();
        return redisServer;
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
      RedisProperties redisProperties) {
        return new LettuceConnectionFactory(
          redisProperties.getRedisHost(), 
          redisProperties.getRedisPort());
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<byte[], byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        return new ReactiveStringRedisTemplate(connectionFactory);
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}
