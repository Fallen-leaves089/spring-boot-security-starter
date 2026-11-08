package io.github.fallenleaves089.security.starter;

import io.github.fallenleaves089.jwt.starter.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test for the aggregated starter: both leaf auto-configurations must load
 * together and keep their documented behavior.
 */
@SpringBootTest(
        classes = SecurityStarterIntegrationTest.TestApplication.class,
        properties = {
                "jwt.secret=test-secret-key-for-security-starter-0123456789abcdef",
                "jwt.exclude-paths=/public/**",
                "security.rate-limit.paths=/api/limited",
                "security.rate-limit.capacity=2",
                "security.rate-limit.window-seconds=60",
                "security.csrf.enabled=false"
        })
@AutoConfigureMockMvc
class SecurityStarterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void publicPathIsReachableWithoutTokenAndGetsSecurityHeaders() throws Exception {
        mockMvc.perform(get("/public/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("public"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Content-Security-Policy",
                        containsString("default-src 'self'")));
    }

    @Test
    void protectedPathRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/private"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.code").value("TOKEN_MISSING"));
    }

    @Test
    void protectedPathAcceptsValidToken() throws Exception {
        String token = jwtUtil.generateToken(42L, "13800000000");

        mockMvc.perform(get("/api/private").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("user=42"));
    }

    @Test
    void rateLimitedPathReturns429AfterCapacity() throws Exception {
        String token = jwtUtil.generateToken(42L, "13800000000");

        mockMvc.perform(get("/api/limited").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/limited").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/limited").header("Authorization", "Bearer " + token))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.code").value(429));
    }

    @SpringBootApplication
    @Import(TestController.class)
    static class TestApplication {
    }

    @RestController
    static class TestController {

        @GetMapping("/public/ping")
        String publicPing() {
            return "public";
        }

        @GetMapping("/api/private")
        String privatePing(HttpServletRequest request) {
            return "user=" + request.getAttribute("userId");
        }

        @GetMapping("/api/limited")
        String limited() {
            return "limited";
        }
    }
}
