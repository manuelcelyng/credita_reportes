package co.com.pragma.crediya.security.config;


import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurity(ServerHttpSecurity http,
                                          Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthConverter) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        // abre lo que deba estar público
                        .pathMatchers(HttpMethod.GET, "/actuator/health").permitAll()

                        // reglas de negocio
                        .pathMatchers(HttpMethod.GET, "/api/v1/reportes").hasAnyAuthority("ADMIN", "ASESOR")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                )
                .build();
    }

    // HS256 decoder con validadores de issuer + audience
    @Bean
    ReactiveJwtDecoder jwtDecoder(
            @Value("${security.jwt.secret}") String secret,
            @Value("${app.issuer}") String issuer,
            @Value("${app.audience}") String audience) {

        byte[] bytes = secret.matches("^[A-Za-z0-9+/=]+$") ? Decoders.BASE64.decode(secret) : secret.getBytes();
        SecretKey key = Keys.hmacShaKeyFor(bytes);

        var decoder = NimbusReactiveJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        // Validadores: issuer, audience y TOLERANCIA de reloj
        OAuth2TokenValidator<Jwt> withIssuer   = JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> withAudience = jwt -> {
            var aud = jwt.getAudience(); // List<String>
            return (aud != null && aud.contains(audience))
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token","audience inválida",""));
        };

        JwtTimestampValidator withSkew = new JwtTimestampValidator(Duration.ofSeconds(120)); // <-- tolerancia

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience, withSkew));
        return decoder;
    }

    // Mapea claim "roles" -> authorities
    @Bean
    Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthConverter() {
        return (Jwt jwt) -> {
            List<String> roles = jwt.getClaimAsStringList("roles");
            var authorities = (roles == null ? List.<String>of() : roles).stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            // principal = sub (userId)
            return Mono.just(new JwtAuthenticationToken(jwt, authorities, jwt.getSubject()));
        };
    }
}
