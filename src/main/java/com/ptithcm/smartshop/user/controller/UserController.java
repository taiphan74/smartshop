package com.ptithcm.smartshop.user.controller;

import com.ptithcm.smartshop.auth.annotation.RequirePermission;
import com.ptithcm.smartshop.auth.enums.AuthPermission;
import com.ptithcm.smartshop.user.dto.UserCreateRequest;
import com.ptithcm.smartshop.user.dto.UserResponse;
import com.ptithcm.smartshop.user.dto.UserUpdateRequest;
import com.ptithcm.smartshop.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	@RequirePermission(AuthPermission.USER_READ)
	public ResponseEntity<List<UserResponse>> findAll() {
		return ResponseEntity.ok(userService.findAll());
	}

	@GetMapping("/{id}")
	@RequirePermission(AuthPermission.USER_READ)
	public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(userService.findById(id));
	}

	@PostMapping
	@RequirePermission(AuthPermission.USER_CREATE)
	public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
	}

	@PutMapping("/{id}")
	@RequirePermission(AuthPermission.USER_UPDATE)
	public ResponseEntity<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request) {
		return ResponseEntity.ok(userService.update(id, request));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RequirePermission(AuthPermission.USER_DELETE)
	public void delete(@PathVariable UUID id) {
		userService.delete(id);
	}
}
