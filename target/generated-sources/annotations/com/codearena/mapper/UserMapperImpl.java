package com.codearena.mapper;

import com.codearena.dto.response.UserResponse;
import com.codearena.entity.Role;
import com.codearena.entity.RoleName;
import com.codearena.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-03T14:05:02+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.1 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.role( userRoleName( user ) );
        userResponse.id( user.getId() );
        userResponse.username( user.getUsername() );
        userResponse.email( user.getEmail() );
        userResponse.fullName( user.getFullName() );
        userResponse.createdAt( user.getCreatedAt() );

        return userResponse.build();
    }

    private RoleName userRoleName(User user) {
        if ( user == null ) {
            return null;
        }
        Role role = user.getRole();
        if ( role == null ) {
            return null;
        }
        RoleName name = role.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
