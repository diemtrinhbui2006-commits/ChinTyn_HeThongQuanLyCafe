package com.example.BuiThiDiemTrinh_TH5.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.example.BuiThiDiemTrinh_TH5.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameIgnoreCase(String username);
}
