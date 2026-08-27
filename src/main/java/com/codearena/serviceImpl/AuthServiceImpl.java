package com.codearena.serviceImpl;

import com.codearena.dto.request.LoginRequest;
import com.codearena.dto.request.RegisterRequest;
import com.codearena.dto.response.AuthResponse;
import com.codearena.entity.Role;
import com.codearena.entity.RoleName;
import com.codearena.entity.User;
import com.codearena.exception.DuplicateResourceException;
import com.codearena.exception.InvalidCredentialsException;
import com.codearena.mapper.UserMapper;
import com.codearena.repository.RoleRepository;
import com.codearena.repository.UserRepository;
import com.codearena.security.JwtService;
import com.codearena.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered");
        }

        // DataInitializer seeds this on startup; if it's genuinely missing the
        // deployment itself is misconfigured, so we fail loudly rather than
        // silently registering a roleless user.
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException(
                        "Default role ROLE_USER is not seeded — check DataInitializer/application startup"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(userRole)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(saved.getUsername());

        return buildAuthResponse(userDetails, saved);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid username/email or password");
        }

        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username/email or password"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        return buildAuthResponse(userDetails, user);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new InvalidCredentialsException("Refresh token is invalid or expired");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        return buildAuthResponse(userDetails, user);
    }

    private AuthResponse buildAuthResponse(UserDetails userDetails, User user) {
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(userMapper.toUserResponse(user))
                .build();
    }
}
