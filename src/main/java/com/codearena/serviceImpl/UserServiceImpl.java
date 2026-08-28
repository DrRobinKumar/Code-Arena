package com.codearena.serviceImpl;

import com.codearena.dto.response.PageResponse;
import com.codearena.dto.response.UserResponse;
import com.codearena.entity.User;
import com.codearena.exception.ResourceNotFoundException;
import com.codearena.mapper.UserMapper;
import com.codearena.repository.UserRepository;
import com.codearena.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> ResourceNotFoundException.of("User", "username", username));
        return userMapper.toUserResponse(user);
    }

    @Override
    public PageResponse<UserResponse> searchUsers(String search, Pageable pageable) {
        String likePattern = (search == null || search.isBlank()) ? null : "%" + search.trim() + "%";
        Page<UserResponse> page = userRepository.search(likePattern, pageable)
                .map(userMapper::toUserResponse);
        return PageResponse.from(page);
    }
}
