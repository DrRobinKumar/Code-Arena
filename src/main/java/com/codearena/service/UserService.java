package com.codearena.service;

import com.codearena.dto.response.PageResponse;
import com.codearena.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse getCurrentUser(String username);

    /** Admin-only listing with pagination, sorting (via Pageable) and free-text search. */
    PageResponse<UserResponse> searchUsers(String search, Pageable pageable);
}
