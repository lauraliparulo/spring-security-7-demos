package de.jugf.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import de.jugf.json.JsonLoginConverter;
import de.jugf.mfa.MfaAuthenticationConverter;
import de.jugf.mfa.MfaRequiredSuccessHandler;
import de.jugf.mfa.MfaSetupHandler;
import de.jugf.mfa.MfaSuccessHandler;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> new CustomUserDetails("admin", "pass123", "");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
            AuthenticationWebFilter loginFilter,
            AuthenticationWebFilter mfaFilter) {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/login","/mfa/setup-2fa").permitAll()
                        .pathMatchers("/mfa/verify").authenticated()
                        .pathMatchers("/mfa").permitAll()
                        .anyExchange().authenticated())
                          .formLogin(form -> form.disable())
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .addFilterAt(loginFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAt(mfaFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public AuthenticationWebFilter loginFilter(MfaSetupHandler mfaSetupHandler) {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(userPasswordAuthManager());

        filter.setServerAuthenticationConverter(new JsonLoginConverter());
        filter.setRequiresAuthenticationMatcher(
                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/login"));
        filter.setAuthenticationSuccessHandler(mfaSetupHandler);

        return filter;
    }

    @Bean
    public AuthenticationWebFilter mfaFilter(MfaRequiredSuccessHandler handler,
            MfaSuccessHandler mfaSuccessHandler) {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(mfaAuthenticationManager(handler));

        filter.setServerAuthenticationConverter(new MfaAuthenticationConverter());
        filter.setRequiresAuthenticationMatcher(
                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/mfa"));

        filter.setAuthenticationSuccessHandler(mfaSuccessHandler);
        return filter;
    }

    public ReactiveAuthenticationManager userPasswordAuthManager() {
        return authentication -> {
            String username = authentication.getName();
            String password = authentication.getCredentials().toString();

            if ("admin".equals(username) && "pass123".equals(password)) {
                return Mono.just(new UsernamePasswordAuthenticationToken(
                        username, password, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
            }
            return Mono.error(new BadCredentialsException("Invalid credentials"));
        };
    }

    public ReactiveAuthenticationManager mfaAuthenticationManager(
            MfaRequiredSuccessHandler handler) {
        return authentication -> {
            String username = authentication.getName();
            String otp = authentication.getCredentials().toString();

            if (handler.validateOtp(username, otp)) {
                return Mono.just(
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))));
            }

            return Mono.error(new BadCredentialsException("Invalid OTP"));
        };
    }

}
