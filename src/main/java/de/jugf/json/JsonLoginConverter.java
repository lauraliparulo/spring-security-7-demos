package de.jugf.json;

import java.nio.charset.StandardCharsets;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class JsonLoginConverter implements ServerAuthenticationConverter {

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return exchange.getRequest().getBody()
                .next()
                .map(dataBuffer -> {
                    try {
                        String body = StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer()).toString();
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode json = mapper.readTree(body);

                        String username = json.get("username").asText();
                        String password = json.get("password").asText();

                        return new UsernamePasswordAuthenticationToken(username, password);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}