# Cache Expiry Spring Boot Starter

Spring Boot application integrates cache with expiry capable quickly.

## Quickstart

- Import dependencies

```xml
    <dependency>
        <groupId>com.yookue.springstarter</groupId>
        <artifactId>cache-expiry-spring-boot-starter</artifactId>
        <version>LATEST</version>
    </dependency>
```

> By default, this starter will auto take effect, you can turn it off by `spring.cache-expiry.enabled = false`

- Configure Spring Boot `application.yml` with prefix `spring.cache-expiry` (**Optional**)

```yml
spring:
    redis:
        host: 'localhost'
        port: 6379
    cache-expiry:
        cache-interceptor:
            inject-cache-manager: true
        cache-manager:
            detect-cache-manager: true
        cache-name-resolver:
            name-prefix: "${spring.application.name}:cache:"
```

- Annotate your (non-static)  method with `@CacheExpiry` annotation, done!

- This starter supports the most popular cache frameworks, including
  - caffeine
  - ehcache
  - jcache
  - redis

## Document

- Github: https://github.com/yookue/cache-expiry-spring-boot-starter

## Requirement

- jdk 1.8+

## License

This project is under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

See the `NOTICE.txt` file for required notices and attributions.

## Donation

You like this package? Then [donate to Yookue](https://yookue.com/public/donate) to support the development.

## Website

- Yookue: https://yookue.com
