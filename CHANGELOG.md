# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- `SECURITY.md` security policy
- `CODE_OF_CONDUCT.md` contributor covenant
- JitPack publishing instructions and dependency coordinates

### Changed

- Refactored the starter into a thin aggregator that depends on `spring-boot-jwt-starter` and `spring-boot-security-filters` instead of duplicating their source files
- Removed local copies of JWT and filter classes; the new `RealIpFilter`, `CsrfTokenFilter`, and `MagicBytesValidator` are now available transitively from `spring-boot-security-filters`

## [1.0.0] - 2026-08-12

### Added

- Merge `spring-boot-jwt-starter` and `spring-boot-security-filters` into a single starter
- JWT auto configuration: `JwtAutoConfiguration`, `JwtUtil` (JJWT 0.12.x, HMAC-SHA), `AuthInterceptor`, `TokenExpiredData`
- Security headers filter: CSP / HSTS / X-Frame-Options / X-Content-Type-Options (8 headers)
- IP-based sliding-window rate limiting: `RateLimitFilter` with 429 responses
- Configurable via `jwt.*` / `security.rate-limit.*` / `security.security-headers.*`
- 11 unit tests (JwtUtil, RateLimitFilter, SecurityHeadersFilter)
