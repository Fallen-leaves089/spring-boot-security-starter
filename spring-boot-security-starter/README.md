# spring-boot-security-starter

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![GitHub](https://img.shields.io/badge/GitHub-Fallen-leaves089%2Fspring--boot--security--starter-lightgrey?logo=github)](https://github.com/Fallen-leaves089/spring-boot-security-starter)
[![Build](https://img.shields.io/github/actions/workflow/status/Fallen-leaves089/spring-boot-security-starter/ci.yml?branch=main&logo=github)](https://github.com/Fallen-leaves089/spring-boot-security-starter/actions)

Spring Boot 3 安全增强 Starter：**JWT 无状态认证 + 安全响应头 + 接口限流**，开箱即用。

MIT License. Copyright (c) 2024 Fallen-leaves089.

> 由 spring-boot-jwt-starter 与 spring-boot-security-filters 合并而来，一套依赖覆盖认证与防护。

---

## 功能特性

| 模块 | 核心类 | 说明 |
|------|--------|------|
| JWT 签发 | `JwtAutoConfiguration` | 自动装配 JWT 全套组件，仅需配置 `jwt.secret` 即可启用 |
| JWT 校验 | `AuthInterceptor` | 拦截请求并校验 Token，自动从请求头解析并注入用户 ID |
| JWT 工具 | `JwtUtil` | 基于 JJWT 0.12.x 的签发/解析，HMAC-SHA 签名 |
| 过期响应 | `TokenExpiredData` | 401 时统一返回 `TOKEN_EXPIRED` 结构，客户端可精准识别 |
| 配置绑定 | `JwtProperties` | `jwt.*` 前缀：secret / expiration / header / excludePaths 等 |
| 安全响应头 | `SecurityHeadersFilter` | 输出 CSP / HSTS / X-Frame-Options 等 8 项安全响应头 |
| 接口限流 | `RateLimitFilter` | 基于 IP 的滑动窗口限流，超限返回 429 |

支持通配符排除路径（`/public/**`、`/api/**` 等 Ant 风格），跨项目复用无需改业务代码。

---

## 快速开始

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

> 适用 Spring Boot 3.2.x + Java 17。

### 最小配置

```yaml
jwt:
  secret: ${JWT_SECRET:dev-please-override}
  expiration: 86400
```

---

## 配置项

### JWT（`jwt.*`）

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `jwt.secret` | 必填 | HMAC-SHA 签名密钥，生产环境务必通过环境变量注入 |
| `jwt.expiration` | `86400` | Token 有效期（秒） |
| `jwt.header` | `Authorization` | 读取 Token 的请求头名称 |
| `jwt.token-prefix` | `Bearer ` | Token 前缀（含空格） |
| `jwt.exclude-paths` | 空 | 免认证路径列表，支持 Ant 通配符 |
| `jwt.user-id-attribute` | `userId` | 解析出的用户 ID 存放的 request attribute 名 |

### 限流（`security.rate-limit.*`）

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `security.rate-limit.enabled` | `true` | 限流总开关 |
| `security.rate-limit.capacity` | `60` | 窗口内最大请求数 |
| `security.rate-limit.window-seconds` | `60` | 窗口大小（秒） |
| `security.rate-limit.paths` | 空 | 需要限流的路径列表（空 = 全部路径） |
| `security.rate-limit.http-status` | `429` | 超限时的响应状态码 |
| `security.rate-limit.message` | `请求过于频繁，请稍后再试` | 超限提示文案 |

### 安全响应头（`security.security-headers.*`）

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `security.security-headers.enabled` | `true` | 安全响应头总开关 |
| `security.security-headers.csp-policy` | `default-src 'self'` | CSP 内容安全策略 |
| `security.security-headers.hsts-enabled` | `true` | 是否启用 HSTS（仅 HTTPS 生效） |
| `security.security-headers.hsts-max-age` | `31536000` | HSTS 有效期（秒） |
| `security.security-headers.prevent-click-jacking` | `true` | 启用 X-Frame-Options: DENY 防点击劫持 |

---

## 验证

```bash
# 验证安全响应头是否生效
curl -sI http://localhost:8080/api/hello | grep -iE 'content-security-policy|x-frame-options|x-content-type-options'

# 验证限流：同一 IP 连续请求 61 次，第 61 次应返回 429
for i in $(seq 1 61); do curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/api/login; done | sort | uniq -c
```

---

## 测试

```bash
mvn -B verify
```

覆盖 JwtUtilTest + SecurityHeadersFilterTest + RateLimitFilterTest。
