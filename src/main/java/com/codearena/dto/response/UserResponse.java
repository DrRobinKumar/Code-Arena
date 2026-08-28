package com.codearena.dto.response;

import com.codearena.entity.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;

    /** Deliberately the enum value, not the internal Role entity — clients need the name, not our FK/id. */
    private RoleName role;
    private LocalDateTime createdAt;
}
