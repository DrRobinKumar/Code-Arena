package com.codearena.service;

import com.codearena.dto.request.LoginRequest;
import com.codearena.dto.request.RegisterRequest;
import com.codearena.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    /** Exchanges a valid refresh token for a fresh access/refresh pair. */
    AuthResponse refresh(String refreshToken);
}
