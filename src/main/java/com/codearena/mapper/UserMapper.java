package com.codearena.mapper;

import com.codearena.dto.response.UserResponse;
import com.codearena.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct generates the implementation at compile time (UserMapperImpl).
 * componentModel = "spring" lets us @Autowired / constructor-inject it like
 * any other bean. unmappedTargetPolicy=IGNORE avoids build failures if the
 * DTO ever has fewer fields than the entity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    // User.role is a Role entity; UserResponse.role is the plain RoleName enum.
    // MapStruct navigates the nested "role.name" path and assigns it directly
    // since the target type already matches RoleName.
    @Mapping(target = "role", source = "role.name")
    UserResponse toUserResponse(User user);
}
