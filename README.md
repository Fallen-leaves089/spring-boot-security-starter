# spring-boot-security-starter

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![GitHub](https://img.shields.io/badge/GitHub-fallen-leaves089%2Fspring--boot--security--starter-lightgrey?logo=github)](https://github.com/fallen-leaves089/spring-boot-security-starter)
[![Build](https://img.shields.io/github/actions/workflow/status/fallen-leaves089/spring-boot-security-starter/ci.yml?branch=main&logo=github)](https://github.com/fallen-leaves089/spring-boot-security-starter/actions)

Spring Boot 3 security aggregator: **stateless JWT authentication + ready-to-use security filters**, bundled as one dependency.

MIT License. Copyright (c) 2024 fallen-leaves089.

> This starter does not duplicate business code. It depends on
> [`spring-boot-jwt-starter`](https://github.com/fallen-leaves089/spring-boot-jwt-starter)
> and
> [`spring-boot-security-filters`](https://github.com/fallen-leaves089/spring-boot-security-filters),
> whose auto-configurations are loaded by Spring Boot.

[中文说明](README.zh-CN.md)

---

## Features

| Module | Provided by | Description |
|--------|-------------|-------------|
| JWT issuing and parsing | `spring-boot-jwt-starter` | Signs and parses tokens with JJWT 0.12.x and HMAC-SHA. |
| JWT request validation | `spring-boot-jwt-starter` | Intercepts requests, validates tokens, and injects `userId`. |
| Path traversal protection | `spring-boot-jwt-starter` | Rejects requests containing `../`, `..\\`, or encoded traversal sequences. |
| Expired-token signaling | `spring-boot-jwt-starter` | Returns a `TOKEN_EXPIRED` marker on 401 responses. |
| Security headers | `spring-boot-security-filters` | Adds CSP / HSTS / X-Frame-Options and other response headers. |
| Rate limiting | `spring-boot-security-filters` | IP-based sliding-window limiting with configurable 429 responses. |
| Trusted-proxy real IP | `spring-boot-security-filters` | Resolves the client IP behind a trusted proxy. |
| Session CSRF tokens | `spring-boot-security-filters` | Protects configurable path prefixes with `X-CSRF-TOKEN` or `_csrf`. |
| Magic-byte validation | `spring-boot-security-filters` | Validates image/video magic bytes against a claimed extension. |

Supports wildcard exclude paths (`/public/**`, `/api/**`, and other Ant-style patterns) for reuse across projects without changing business code.

---

## Dependency coordinates

This project is published through [JitPack](https://jitpack.io). Add the JitPack repository first.

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.fallen-leaves089</groupId>
    <artifactId>spring-boot-security-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```gradle
dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
    }
}

dependencies {
    implementation("com.github.fallen-leaves089:spring-boot-security-starter:1.0.0")
}
```

> Requires Spring Boot 3.2.x and Java 17.

---

## Quick start

```yaml
jwt:
  secret: ${JWT_SECRET:dev-please-override}
  expiration: 86400

security:
  rate-limit:
    paths:
      - /api/login
      - /api/sms/*
  security-headers:
    enabled: true
```

No `@ComponentScan` or extra Java code is required.

---

## Configuration

### JWT (`jwt.*`)

| Property | Default | Description |
|----------|---------|-------------|
| `jwt.secret` | required | HMAC-SHA signing secret. In production, always inject it via an environment variable. |
| `jwt.expiration` | `86400` | Token validity in seconds. |
| `jwt.header` | `Authorization` | HTTP header used to read the token. |
| `jwt.token-prefix` | `Bearer ` | Token prefix, including the trailing space. |
| `jwt.exclude-paths` | empty | Paths excluded from authentication. Supports `*` wildcards. |
| `jwt.user-id-attribute` | `userId` | Request attribute name where the parsed user ID is stored. |

### Rate limiting (`security.rate-limit.*`)

| Property | Default | Description |
|----------|---------|-------------|
| `security.rate-limit.enabled` | `true` | Master switch for rate limiting. |
| `security.rate-limit.paths` | empty | Path patterns to rate limit. |
| `security.rate-limit.capacity` | `60` | Maximum requests per IP within the window. |
| `security.rate-limit.window-seconds` | `60` | Window size in seconds. |
| `security.rate-limit.http-status` | `429` | HTTP status returned when the limit is exceeded. |
| `security.rate-limit.message` | `请求过于频繁，请稍后再试` | Message returned when the limit is exceeded. |

### Security headers (`security.security-headers.*`)

| Property | Default | Description |
|----------|---------|-------------|
| `security.security-headers.enabled` | `true` | Master switch for security headers. |
| `security.security-headers.csp-policy` | `default-src 'self'` | Content-Security-Policy value. |
| `security.security-headers.hsts-enabled` | `true` | Whether to enable HSTS. Only applies to HTTPS requests. |
| `security.security-headers.hsts-max-age` | `31536000` | HSTS validity in seconds. |
| `security.security-headers.prevent-click-jacking` | `true` | Enables `X-Frame-Options: DENY` to prevent clickjacking. |

Additional `security.real-ip.*` and `security.csrf.*` options are documented in `spring-boot-security-filters`.

---

## Verification

```bash
# Check that security headers are added
curl -sI http://localhost:8080/api/hello | grep -iE 'content-security-policy|x-frame-options|x-content-type-options'

# Trigger rate limiting: send 61 requests from the same IP; the 61st should return 429
for i in $(seq 1 61); do curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/api/login; done | sort | uniq -c
```

---

## Building

```bash
mvn -B verify
```

This aggregator intentionally keeps only a marker class. The behavior tests live in the two dependency repositories.

---

## Releasing

JitPack builds a release automatically from a Git tag.

```bash
git tag 1.0.0
git push origin 1.0.0
```

Then use:

```text
https://jitpack.io/#fallen-leaves089/spring-boot-security-starter/1.0.0
```

Maven Central publishing requires OSSRH credentials, signed artifacts, and source/javadoc jars. The two dependency starters must be published before this aggregator can resolve from Maven Central.
