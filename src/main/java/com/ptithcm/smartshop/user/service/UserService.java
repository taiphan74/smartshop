package com.ptithcm.smartshop.user.service;

import com.ptithcm.smartshop.user.dto.UserCreateRequest;
import com.ptithcm.smartshop.user.dto.UserResponse;
import com.ptithcm.smartshop.user.dto.UserUpdateRequest;
import java.util.List;
import java.util.UUID;

public interface UserService {

	List<UserResponse> findAll();

	UserResponse findById(UUID id);

	UserResponse create(UserCreateRequest request);

	UserResponse update(UUID id, UserUpdateRequest request);

	void delete(UUID id);
}
