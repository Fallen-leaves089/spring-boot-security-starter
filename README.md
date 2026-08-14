# spring-boot-security-starter

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![GitHub](https://img.shields.io/badge/GitHub-fallen-leaves089%2Fspring--boot--security--starter-lightgrey?logo=github)](https://github.com/fallen-leaves089/spring-boot-security-starter)
[![Build](https://img.shields.io/github/actions/workflow/status/fallen-leaves089/spring-boot-security-starter/ci.yml?branch=main&logo=github)](https://github.com/fallen-leaves089/spring-boot-security-starter/actions)

Spring Boot 3 security starter: **stateless JWT authentication + security headers + API rate limiting**, ready to use out of the box.

MIT License. Copyright (c) 2024 fallen-leaves089.

> This starter merges `spring-boot-jwt-starter` and `spring-boot-security-filters` into a single dependency for authentication and protection.

[中文说明](README.zh-CN.md)

---

## Features

| Module | Core class | Description |
|--------|------------|-------------|
| JWT issuing | `JwtAutoConfiguration` | Auto-configures the complete JWT component set; only `jwt.secret` is required |
| JWT verification | `AuthInterceptor` | Intercepts requests, validates tokens, parses the request header, and injects the user ID |
| JWT utility | `JwtUtil` | Signs and parses tokens with JJWT 0.12.x and HMAC-SHA |
| Expired response | `TokenExpiredData` | Returns a unified `TOKEN_EXPIRED` structure on 401 so clients can detect expiry |
| Configuration binding | `JwtProperties` | `jwt.*` prefix: `secret` / `expiration` / `header` / `excludePaths`, etc. |
| Security headers | `SecurityHeadersFilter` | Adds 8 HTTP security headers such as CSP / HSTS / X-Frame-Options |
| Rate limiting | `RateLimitFilter` | IP-based sliding-window rate limiting; returns 429 when the limit is exceeded |

Supports wildcard exclude paths (`/public/**`, `/api/**`, and other Ant-style patterns) for reuse across projects without changing business code.

---

## Quick start

### Maven

```xml
<dependency>
    <groupId>io.github.fallenleaves089</groupId>
    <artifactId>spring-boot-security-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'io.github.fallenleaves089:spring-boot-security-starter:1.0.0'
```

> Requires Spring Boot 3.2.x and Java 17.

### Minimal configuration

```yaml
jwt:
  secret: ${JWT_SECRET:dev-please-override}
  expiration: 86400
```

---

## Configuration

### JWT (`jwt.*`)

| Property | Default | Description |
|----------|---------|-------------|
| `jwt.secret` | required | HMAC-SHA signing secret. In production, always inject it via an environment variable. |
| `jwt.expiration` | `86400` | Token validity in seconds. |
| `jwt.header` | `Authorization` | HTTP header used to read the token. |
| `jwt.token-prefix` | `Bearer ` | Token prefix, including the trailing space. |
| `jwt.exclude-paths` | empty | Paths excluded from authentication. Supports Ant-style wildcards. |
| `jwt.user-id-attribute` | `userId` | Request attribute name where the parsed user ID is stored. |

### Rate limiting (`security.rate-limit.*`)

| Property | Default | Description |
|----------|---------|-------------|
| `security.rate-limit.enabled` | `true` | Master switch for rate limiting. |
| `security.rate-limit.capacity` | `60` | Maximum requests per IP within the window. |
| `security.rate-limit.window-seconds` | `60` | Window size in seconds. |
| `security.rate-limit.paths` | empty | Path patterns to rate limit. An empty list applies to all paths. |
| `security.rate-limit.http-status` | `429` | HTTP status returned when the limit is exceeded. |
| `security.rate-limit.message` | `请求过于频繁，请稍后再试` | Message returned when the limit is exceeded. The default is a Chinese string; override it for other locales. |

### Security headers (`security.security-headers.*`)

| Property | Default | Description |
|----------|---------|-------------|
| `security.security-headers.enabled` | `true` | Master switch for security headers. |
| `security.security-headers.csp-policy` | `default-src 'self'` | Content-Security-Policy value. |
| `security.security-headers.hsts-enabled` | `true` | Whether to enable HSTS. Only applies to HTTPS requests. |
| `security.security-headers.hsts-max-age` | `31536000` | HSTS validity in seconds. |
| `security.security-headers.prevent-click-jacking` | `true` | Enables `X-Frame-Options: DENY` to prevent clickjacking. |

---

## Verification

```bash
# Check that security headers are added
curl -sI http://localhost:8080/api/hello | grep -iE 'content-security-policy|x-frame-options|x-content-type-options'

# Trigger rate limiting: send 61 requests from the same IP; the 61st should return 429
for i in $(seq 1 61); do curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/api/login; done | sort | uniq -c
```

---

## Tests

```bash
mvn -B verify
```

Covers `JwtUtilTest`, `SecurityHeadersFilterTest`, and `RateLimitFilterTest`.
