package com.paulo.independentschooldata.mappers;

import com.paulo.independentschooldata.domain.User;
import com.paulo.independentschooldata.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(user.getId(), user.getEmail());
    }
}