package be.kdg.sa.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class SecurityTestConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        Jwt jwt = new Jwt(
                "mock-token",
                null,
                null,
                Map.of("alg", "none"),
                Map.of(
                        "sub", UUID.randomUUID().toString(),
                        "preferred_username", "owner",
                        "realm_access", Map.of("roles", List.of("OWNER"))
                )
        );

        JwtDecoder decoder = mock(JwtDecoder.class);
        when(decoder.decode("mock-token")).thenReturn(jwt);
        return decoder;
    }
}
