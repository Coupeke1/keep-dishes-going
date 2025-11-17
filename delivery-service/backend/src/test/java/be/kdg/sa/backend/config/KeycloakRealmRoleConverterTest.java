package be.kdg.sa.backend.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

    @Mock
    private Jwt jwt;

    @Test
    void convert_shouldReturnOwnerAndCouriersRoles() {
        // Given
        List<String> roles = List.of("owner", "couriers");
        Map<String, Object> realmAccess = Map.of("roles", roles);
        when(jwt.getClaims()).thenReturn(Map.of("realm_access", realmAccess));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities)
                .hasSize(2)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_owner", "ROLE_couriers");
    }

    @Test
    void convert_shouldReturnOnlyOwnerRole() {
        // Given
        List<String> roles = List.of("owner");
        Map<String, Object> realmAccess = Map.of("roles", roles);
        when(jwt.getClaims()).thenReturn(Map.of("realm_access", realmAccess));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities)
                .hasSize(1)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_owner");
    }

    @Test
    void convert_shouldReturnOnlyCouriersRole() {
        // Given
        List<String> roles = List.of("couriers");
        Map<String, Object> realmAccess = Map.of("roles", roles);
        when(jwt.getClaims()).thenReturn(Map.of("realm_access", realmAccess));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities)
                .hasSize(1)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_couriers");
    }

    @Test
    void convert_shouldHandleMixedValidAndInvalidRoles() {
        // Given
        List<String> roles = Arrays.asList("owner", null, "", "couriers", "  ");
        Map<String, Object> realmAccess = Map.of("roles", roles);
        when(jwt.getClaims()).thenReturn(Map.of("realm_access", realmAccess));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities)
                .hasSize(2)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_owner", "ROLE_couriers");
    }

    @Test
    void convert_shouldWorkWithRealJwtStructureFromKeycloak() {
        // Given
        Map<String, Object> realmAccess = Map.of(
                "roles", List.of("owner", "couriers")
        );
        when(jwt.getClaims()).thenReturn(Map.of(
                "realm_access", realmAccess,
                "sub", "123e4567-e89b-12d3-a456-426614174000",
                "email_verified", true,
                "preferred_username", "restaurant_owner",
                "email", "owner@restaurant.com"
        ));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities)
                .hasSize(2)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_owner", "ROLE_couriers");
    }

    @Test
    void convert_shouldReturnEmptyListWhenClaimsNull() {
        when(jwt.getClaims()).thenReturn(null);
        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        assertThat(authorities).isEmpty();
    }

    @Test
    void convert_shouldReturnEmptyListWhenRealmAccessNotMap() {
        when(jwt.getClaims()).thenReturn(Map.of("realm_access", "not-a-map"));
        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        assertThat(authorities).isEmpty();
    }

    @Test
    void convert_shouldReturnEmptyListWhenRolesNotList() {
        Map<String, Object> realmAccess = Map.of("roles", "not-a-list");
        when(jwt.getClaims()).thenReturn(Map.of("realm_access", realmAccess));
        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        assertThat(authorities).isEmpty();
    }

    @Test
    void convert_shouldReturnEmptyListWhenRolesEmpty() {
        Map<String, Object> realmAccess = Map.of("roles", List.of());
        when(jwt.getClaims()).thenReturn(Map.of("realm_access", realmAccess));
        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        assertThat(authorities).isEmpty();
    }
}