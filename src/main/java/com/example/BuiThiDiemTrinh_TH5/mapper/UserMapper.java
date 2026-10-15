package com.example.BuiThiDiemTrinh_TH5.mapper;

import org.springframework.stereotype.Component;

import com.example.BuiThiDiemTrinh_TH5.dto.UserRequest;
import com.example.BuiThiDiemTrinh_TH5.dto.UserResponse;
import com.example.BuiThiDiemTrinh_TH5.entity.User;

@Component
public class UserMapper {

    public User toEntity(UserRequest request) {
        if (request == null) {
            return null;
        }
        return new User(null, request.username(), request.password(), request.role(), null);
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
