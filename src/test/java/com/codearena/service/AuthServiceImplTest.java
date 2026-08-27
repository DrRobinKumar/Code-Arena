package com.codearena.service;

import com.codearena.dto.request.RegisterRequest;
import com.codearena.dto.response.AuthResponse;
import com.codearena.dto.response.UserResponse;
import com.codearena.entity.Role;
import com.codearena.entity.RoleName;
import com.codearena.entity.User;
import com.codearena.exception.DuplicateResourceException;
import com.codearena.mapper.UserMapper;
import com.codearena.repository.RoleRepository;
import com.codearena.repository.UserRepository;
import com.codearena.security.JwtService;
import com.codearena.serviceImpl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private User savedUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("john_doe", "john@example.com", "SuperSecret123", "John Doe");
        userRole = Role.builder().name(RoleName.ROLE_USER).description("Standard platform user").build();
        savedUser = User.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("hashed")
                .fullName("John Doe")
                .role(userRole)
                .enabled(true)
                .build();
    }

    @Test
    void register_shouldCreateUserAndReturnTokens_whenUsernameAndEmailAreUnique() {
        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("SuperSecret123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userDetailsService.loadUserByUsername("john_doe")).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh-token");
        when(userMapper.toUserResponse(savedUser)).thenReturn(
                UserResponse.builder().id(1L).username("john_doe").email("john@example.com")
                        .role(RoleName.ROLE_USER).build());

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser().getUsername()).isEqualTo("john_doe");
        assertThat(response.getUser().getRole()).isEqualTo(RoleName.ROLE_USER);
    }

    @Test
    void register_shouldThrowDuplicateResourceException_whenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username");
    }

    @Test
    void register_shouldThrowDuplicateResourceException_whenEmailAlreadyExists() {
        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void register_shouldThrowIllegalStateException_whenDefaultRoleIsNotSeeded() {
        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ROLE_USER");
    }
}
