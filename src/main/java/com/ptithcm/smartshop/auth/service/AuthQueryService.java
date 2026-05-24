package com.ptithcm.smartshop.auth.service;

import com.ptithcm.smartshop.shared.exception.ConflictException;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthQueryService {

    private final UserRepository userRepository;

    public AuthQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ConflictException("User not found"));
    }
}
