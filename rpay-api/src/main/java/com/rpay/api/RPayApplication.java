package com.rpay.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application for RPay Payment Gateway.
 *
 * Features enabled:
 * - Virtual Threads (Java 21) for high concurrency
 * - Async processing for non-blocking operations
 * - Caching for improved performance
 * - Scheduled tasks for background jobs
 * - JPA for database access
 *
 * Performance considerations:
 * - Uses virtual threads to handle millions of concurrent requests
 * - Connection pooling configured for optimal throughput
 * - Multi-level caching strategy
 * - Async event processing
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableCaching
@ComponentScan(basePackages = "com.rpay")
@EnableJpaRepositories(basePackages = "com.rpay.infrastructure.persistence.repository")
@EntityScan(basePackages = "com.rpay.infrastructure.persistence.entity")
public class RPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(RPayApplication.class, args);
    }
}
