package io.github.fallenleaves089.security.starter;

/**
 * Marker for the aggregated Spring Boot security starter.
 *
 * <p>This project intentionally contains no duplicated authentication or filter code.
 * It depends on {@code spring-boot-jwt-starter} and {@code spring-boot-security-filters},
 * whose auto-configurations are loaded automatically by Spring Boot.</p>
 */
public final class SecurityStarterMarker {

    private SecurityStarterMarker() {
        // Prevent instantiation.
    }
}
