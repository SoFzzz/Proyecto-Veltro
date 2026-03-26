package com.veltro.inventory.infrastructure.adapters.security;

import com.veltro.inventory.domain.iam.model.UserEntity;
import com.veltro.inventory.domain.iam.ports.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security {@link UserDetailsService} adapter.
 *
 * Loads a user from the domain port {@link UserRepository} and converts it into
 * a Spring Security {@link UserDetails} object. Only active users are returned
 * (soft-delete AC-05).
 *
 * The granted authority follows the {@code ROLE_<ROLE>} Spring Security convention.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> {
                    log.debug("User not found or inactive: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        return User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
    }
}
