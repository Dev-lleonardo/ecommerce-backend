package com.seuprojeto.ecommerce.auth;

import com.seuprojeto.ecommerce.auth.dto.LoginRequestDTO;
import com.seuprojeto.ecommerce.auth.dto.RegisterRequestDTO;
import com.seuprojeto.ecommerce.exception.BusinessException;
import com.seuprojeto.ecommerce.role.Role;
import com.seuprojeto.ecommerce.role.RoleRepository;
import com.seuprojeto.ecommerce.security.CustomUserDetailsService;
import com.seuprojeto.ecommerce.security.JwtService;
import com.seuprojeto.ecommerce.user.User;
import com.seuprojeto.ecommerce.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        RegisterRequestDTO dto = new RegisterRequestDTO("Leo", "leo@email.com", "123456");
        when(userRepository.existsByEmail(dto.email())).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(dto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerShouldSaveUserWithEncodedPasswordAndRoleUser() {
        RegisterRequestDTO dto = new RegisterRequestDTO("Leo", "leo@email.com", "123456");
        Role roleUser = Role.builder().id(1L).name("ROLE_USER").build();

        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode(dto.password())).thenReturn("encoded-password");

        authService.register(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        assertEquals("Leo", savedUser.getName());
        assertEquals("leo@email.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(Set.of(roleUser), savedUser.getRoles());
    }

    @Test
    void loginShouldThrowWhenPasswordDoesNotMatch() {
        LoginRequestDTO dto = new LoginRequestDTO("leo@email.com", "wrong-pass");
        User user = User.builder()
                .email("leo@email.com")
                .password("encoded-password")
                .build();

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(false);

        assertThrows(BusinessException.class, () -> authService.login(dto));
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() {
        LoginRequestDTO dto = new LoginRequestDTO("leo@email.com", "123456");
        User user = User.builder()
                .email("leo@email.com")
                .password("encoded-password")
                .build();
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("leo@email.com")
                .password("encoded-password")
                .authorities("ROLE_USER")
                .build();

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(true);
        when(userDetailsService.loadUserByUsername("leo@email.com")).thenReturn(userDetails);
        when(jwtService.generateToken(user)).thenReturn("fake-jwt-token");

        var response = authService.login(dto);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.token());
    }
}
