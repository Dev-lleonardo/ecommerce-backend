package com.seuprojeto.ecommerce.config;

import com.seuprojeto.ecommerce.role.Role;
import com.seuprojeto.ecommerce.role.RoleRepository;
import com.seuprojeto.ecommerce.user.User;
import com.seuprojeto.ecommerce.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name}")
    private String adminName;

    @Bean
    CommandLineRunner initData() {
        return args -> {

            Role roleUser = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> {
                        log.info("Criando role ROLE_USER...");
                        return roleRepository.save(new Role(null, "ROLE_USER"));
                    });

            Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> {
                        log.info("Criando role ROLE_ADMIN...");
                        return roleRepository.save(new Role(null, "ROLE_ADMIN"));
                    });

            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = User.builder()
                        .name(adminName)
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .roles(Set.of(roleAdmin, roleUser))
                        .build();

                userRepository.save(admin);
                log.info("Admin padrão criado → email: {}", adminEmail);
            }
        };
    }
}