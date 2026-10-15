package com.example.BuiThiDiemTrinh_TH5.service;

import java.util.List;

import com.example.BuiThiDiemTrinh_TH5.entity.User;

public interface UserService {

    User create(User user);

    List<User> getAllUsers();

    User getUserById(Long id);

    User update(Long id, User user);

    void delete(Long id);
}
