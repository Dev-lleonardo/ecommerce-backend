package com.seuprojeto.ecommerce.auth;

import com.seuprojeto.ecommerce.auth.dto.LoginRequestDTO;
import com.seuprojeto.ecommerce.auth.dto.LoginResponseDTO;
import com.seuprojeto.ecommerce.auth.dto.RegisterRequestDTO;
import com.seuprojeto.ecommerce.exception.BusinessException;
import com.seuprojeto.ecommerce.role.Role;
import com.seuprojeto.ecommerce.role.RoleRepository;
import com.seuprojeto.ecommerce.security.CustomUserDetailsService;
import com.seuprojeto.ecommerce.security.JwtService;
import com.seuprojeto.ecommerce.user.User;
import com.seuprojeto.ecommerce.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public void register(RegisterRequestDTO dto) {

        if (userRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Email já cadastrado");
        }

        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new BusinessException("Role USER não encontrada"));

        User user = User.builder()
                .name(dto.name())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .roles(Set.of(roleUser))
                .build();

        userRepository.save(user);
    }

    public LoginResponseDTO login(LoginRequestDTO dto) {

        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new BusinessException("Email ou senha inválidos"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new BusinessException("Email ou senha inválidos");
        }

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getEmail());

        String token = jwtService.generateToken(user);

        return new LoginResponseDTO(token);
    }
}
