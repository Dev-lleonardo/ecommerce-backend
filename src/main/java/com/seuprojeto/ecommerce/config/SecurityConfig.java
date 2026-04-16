package com.seuprojeto.ecommerce.config;

import com.seuprojeto.ecommerce.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desabilita CSRF, essencial para APIs REST que usam JWT
                .csrf(AbstractHttpConfigurer::disable)

                // Configura a autenticação como STATELESS (sem estado), pois usaremos Tokens
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Define as regras de permissão das rotas
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",    // ADICIONADO O PREFIXO /api PARA BATER COM O POSTMAN
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()              // Estas rotas não precisam de Token
                        .anyRequest().authenticated() // Qualquer outra rota exige login
                )

                // Adiciona seu filtro JWT antes do filtro padrão de usuário/senha
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}