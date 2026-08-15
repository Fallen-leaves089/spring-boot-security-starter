# spring-boot-security-starter

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![GitHub](https://img.shields.io/badge/GitHub-fallen-leaves089%2Fspring--boot--security--starter-lightgrey?logo=github)](https://github.com/fallen-leaves089/spring-boot-security-starter)
[![Build](https://img.shields.io/github/actions/workflow/status/fallen-leaves089/spring-boot-security-starter/ci.yml?branch=main&logo=github)](https://github.com/fallen-leaves089/spring-boot-security-starter/actions)

Spring Boot 3 安全增强聚合 Starter：**JWT 无状态认证 + 开箱即用的安全过滤器**，一套依赖覆盖认证与防护。

MIT License. Copyright (c) 2024 fallen-leaves089.

> 本项目不再复制业务代码，而是依赖
> [`spring-boot-jwt-starter`](https://github.com/fallen-leaves089/spring-boot-jwt-starter)
> 与
> [`spring-boot-security-filters`](https://github.com/fallen-leaves089/spring-boot-security-filters)，
> 由 Spring Boot 自动加载它们的自动配置。

---

## 功能特性

| 模块 | 由哪个依赖提供 | 说明 |
|------|----------------|------|
| JWT 签发与解析 | `spring-boot-jwt-starter` | 基于 JJWT 0.12.x 与 HMAC-SHA 签名/解析。 |
| JWT 请求校验 | `spring-boot-jwt-starter` | 拦截请求、校验 Token 并注入 `userId`。 |
| 路径遍历防护 | `spring-boot-jwt-starter` | 拒绝包含 `../`、`..\\` 或编码遍历序列的请求。 |
| 过期响应标记 | `spring-boot-jwt-starter` | 401 响应中携带 `TOKEN_EXPIRED` 标识。 |
| 安全响应头 | `spring-boot-security-filters` | 输出 CSP / HSTS / X-Frame-Options 等响应头。 |
| 接口限流 | `spring-boot-security-filters` | 基于 IP 的滑动窗口限流，超限返回 429。 |
| 可信代理真实 IP | `spring-boot-security-filters` | 解析可信代理后的客户端 IP。 |
| Session CSRF | `spring-boot-security-filters` | 使用 `X-CSRF-TOKEN` 或 `_csrf` 保护路径前缀。 |
| Magic Bytes 校验 | `spring-boot-security-filters` | 校验图片/视频文件魔数与扩展名是否一致。 |

支持通配符排除路径（`/public/**`、`/api/**` 等 Ant 风格），跨项目复用无需改业务代码。

---

## 依赖坐标

本项目通过 [JitPack](https://jitpack.io) 发布，使用时先添加 JitPack 仓库。

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

> 适用 Spring Boot 3.2.x + Java 17。

---

## 快速开始

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

无需额外 Java 代码或 `@ComponentScan`。

---

## 配置项

### JWT（`jwt.*`）

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `jwt.secret` | 必填 | HMAC-SHA 签名密钥，生产环境务必通过环境变量注入 |
| `jwt.expiration` | `86400` | Token 有效期（秒） |
| `jwt.header` | `Authorization` | 读取 Token 的请求头名称 |
| `jwt.token-prefix` | `Bearer ` | Token 前缀（含空格） |
| `jwt.exclude-paths` | 空 | 免认证路径列表，支持 `*` 通配符 |
| `jwt.user-id-attribute` | `userId` | 解析出的用户 ID 存放的 request attribute 名 |

### 限流（`security.rate-limit.*`）

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `security.rate-limit.enabled` | `true` | 限流总开关 |
| `security.rate-limit.paths` | 空 | 需要限流的路径列表 |
| `security.rate-limit.capacity` | `60` | 窗口内最大请求数 |
| `security.rate-limit.window-seconds` | `60` | 窗口大小（秒） |
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

`security.real-ip.*` 与 `security.csrf.*` 的更多配置见 `spring-boot-security-filters`。

---

## 验证

```bash
# 验证安全响应头是否生效
curl -sI http://localhost:8080/api/hello | grep -iE 'content-security-policy|x-frame-options|x-content-type-options'

# 验证限流：同一 IP 连续请求 61 次，第 61 次应返回 429
for i in $(seq 1 61); do curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/api/login; done | sort | uniq -c
```

---

## 构建

```bash
mvn -B verify
```

本项目只保留一个标记类，具体行为测试位于两个依赖仓库中。

---

## 发布

JitPack 会根据 Git tag 自动构建发布。

```bash
git tag 1.0.0
git push origin 1.0.0
```

随后可使用：

```text
https://jitpack.io/#fallen-leaves089/spring-boot-security-starter/1.0.0
```

发布到 Maven Central 需要 OSSRH 凭据、签名制品以及 source/javadoc jar；且必须先发布两个依赖 Starter。
