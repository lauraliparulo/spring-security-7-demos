package de.jugf.mfa;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import de.jugf.dto.MfaRequest;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

public class MfaAuthenticationConverter implements ServerAuthenticationConverter {

    private final ObjectMapper mapper = new ObjectMapper();

    // @Override
    // public Mono<Authentication> convert(ServerWebExchange exchange) {
    //     return DataBufferUtils.join(exchange.getRequest().getBody())
    //             .map(buffer -> {
    //                 try {
    //                     String body = StandardCharsets.UTF_8
    //                             .decode(buffer.asByteBuffer()).toString();

    //                     JsonNode json = mapper.readTree(body);

    //                     String username = json.get("username").asText();
    //                     String otp = json.get("otp").asText();

    //                     return new UsernamePasswordAuthenticationToken(username, otp);

    //                 } catch (Exception e) {
    //                     throw new RuntimeException("Invalid MFA JSON", e);
    //                 }
    //             });
    // }

    
    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {

        return ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders())
            .bodyToMono(MfaRequest.class)
            .map(req -> new UsernamePasswordAuthenticationToken(
                    req.username(),
                    req.otp()
            ));
    }

}
