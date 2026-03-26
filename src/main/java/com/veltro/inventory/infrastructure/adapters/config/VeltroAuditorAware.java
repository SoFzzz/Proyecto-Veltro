package com.veltro.inventory.infrastructure.adapters.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Supplies the current auditor username to Spring Data JPA auditing.
 *
 * Resolution order:
 *   1. Authenticated, non-anonymous principal → use principal name.
 *   2. No security context or anonymous → fall back to "SYSTEM" so that
 *      batch jobs and bootstrap data are still properly attributed (ADR-004).
 */
public class VeltroAuditorAware implements AuditorAware<String> {

    private static final String SYSTEM_USER = "SYSTEM";

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of(SYSTEM_USER);
        }

        return Optional.of(authentication.getName());
    }
}
