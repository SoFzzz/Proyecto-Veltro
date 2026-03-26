package com.veltro.inventory.infrastructure.adapters.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Activates Spring Data JPA auditing and registers the auditor-aware bean.
 *
 * The auditorAwareRef must match the bean name returned by {@link #veltroAuditorAware()}.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "veltroAuditorAware")
public class AuditConfig {

    @Bean
    public AuditorAware<String> veltroAuditorAware() {
        return new VeltroAuditorAware();
    }
}
