package com.ptithcm.smartshop.common.exception;

import com.ptithcm.smartshop.common.api.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception) {
		return build(HttpStatus.NOT_FOUND, exception.getMessage(), List.of());
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException exception) {
		return build(HttpStatus.CONFLICT, exception.getMessage(), List.of());
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException exception) {
		return build(HttpStatus.UNAUTHORIZED, exception.getMessage(), List.of());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException exception) {
		return build(HttpStatus.FORBIDDEN, exception.getMessage(), List.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		List<String> details = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(FieldError::getDefaultMessage)
			.toList();
		return build(HttpStatus.BAD_REQUEST, "Validation failed", details);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
		List<String> details = exception.getConstraintViolations()
			.stream()
			.map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
			.toList();
		return build(HttpStatus.BAD_REQUEST, "Validation failed", details);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleGeneral(Exception exception) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), List.of());
	}

	private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, List<String> details) {
		return ResponseEntity.status(status)
			.body(new ApiErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, details));
	}
}
