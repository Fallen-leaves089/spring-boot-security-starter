# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.2.0] - 2026-10-27

### Changed

- Depend on `spring-boot-security-filters` 1.1.0 and `spring-boot-jwt-starter` 1.1.1.
- Inherit the trusted-proxy rate-limit fix, lazy counter eviction, stable CSRF token contract, JWT secret validation, and encoded-traversal hardening from the leaf starters.
- Document that the aggregator enables the leaf defaults and that leaf tags must be published before the aggregator tag.

## [1.1.0] - 2026-10-26

### Added

- `SECURITY.md` security policy
- `CODE_OF_CONDUCT.md` contributor covenant
- JitPack publishing instructions and dependency coordinates

### Changed

- Refactored the starter into a thin aggregator that depends on `spring-boot-jwt-starter` and `spring-boot-security-filters` instead of duplicating their source files
- Removed local copies of JWT and filter classes; the new `RealIpFilter`, `CsrfTokenFilter`, and `MagicBytesValidator` are now available transitively from `spring-boot-security-filters`
- README adds a 30-second overview, shortest demo, and guidance for choosing the aggregator vs a leaf starter
- Quick start now includes `jwt.exclude-paths`; JJWT references are aligned with 0.13.x
- Release tags are documented as `v1.1.0`; the JitPack coordinate is `1.1.0`

## [1.0.0] - 2026-08-12

### Added

- Merge `spring-boot-jwt-starter` and `spring-boot-security-filters` into a single starter
- JWT auto configuration: `JwtAutoConfiguration`, `JwtUtil` (JJWT 0.13.x, HMAC-SHA), `AuthInterceptor`, `TokenExpiredData`
- Security headers filter: CSP / HSTS / X-Frame-Options / X-Content-Type-Options (8 headers)
- IP-based rate limiting: `RateLimitFilter` with 429 responses
- Configurable via `jwt.*` / `security.rate-limit.*` / `security.security-headers.*`
- 11 unit tests (JwtUtil, RateLimitFilter, SecurityHeadersFilter)
