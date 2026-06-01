package com.seuprojeto.ecommerce.user;

import com.seuprojeto.ecommerce.exception.BusinessException;
import com.seuprojeto.ecommerce.exception.ResourceNotFoundException;
import com.seuprojeto.ecommerce.role.Role;
import com.seuprojeto.ecommerce.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO register(UserRequestDTO dto) {

        if (userRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Email já cadastrado");
        }

        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role não encontrada")
                );

        User user = User.builder()
                .name(dto.name())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .roles(Set.of(roleUser))
                .build();

        userRepository.save(user);

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
    public UserResponseDTO findByEmail(String email) {
        // 1. Busca o usuário no seu UserRepository pelo email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado com o e-mail: " + email)
                );

        // 2. Converte a entidade 'User' para o 'UserResponseDTO' usando o mesmo padrão do seu return acima
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}

