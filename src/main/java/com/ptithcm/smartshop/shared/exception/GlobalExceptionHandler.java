package com.ptithcm.smartshop.shared.exception;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
/**
 * Bộ xử lý exception toàn cục cho toàn bộ API/web layer.
 *
 * Mục tiêu:
 * - Chuẩn hóa format lỗi trả về client.
 * - Mapping đúng HTTP status theo từng loại lỗi nghiệp vụ/kỹ thuật.
 * - Ghi log tập trung với lỗi không kiểm soát.
 */
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Xử lý lỗi validate DTO từ @Valid (request body/form).
     *
     * @param ex lỗi validate chứa danh sách field lỗi
     * @return payload 400 gồm timestamp, status, error và map field lỗi
     */
    // Gom lỗi validate DTO thành map field -> message để client hiển thị theo từng
    // input.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // BƯỚC 1: Khởi tạo response body chung.
        Map<String, Object> body = new HashMap<>();

        // BƯỚC 2: Điền metadata chuẩn cho lỗi 400.
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("error", "Bad Request");

        // BƯỚC 3: Chuyển danh sách FieldError -> Map<field, message> để client bind dễ
        // dàng.
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage()
                                : "Invalid value",
                        (existing, replacement) -> existing));

        // BƯỚC 4: Gắn chi tiết lỗi vào body và trả response.
        body.put("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Xử lý lỗi không tìm thấy tài nguyên.
     *
     * @param ex lỗi ResourceNotFoundException
     * @return payload 404 có message chi tiết
     */
    // Trả về 404 khi tài nguyên không tồn tại.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        // BƯỚC 1: Khởi tạo body.
        Map<String, Object> body = new HashMap<>();

        // BƯỚC 2: Điền metadata cho lỗi 404.
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 404);
        body.put("error", "Not Found");

        // BƯỚC 3: Gắn message nghiệp vụ và trả kết quả.
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Xử lý lỗi input không hợp lệ theo nghiệp vụ (không phải lỗi @Valid).
     *
     * @param ex lỗi BadRequestException
     * @return payload 400
     */
    // Trả về 400 cho các lỗi nghiệp vụ do input không hợp lệ.
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException ex) {
        // BƯỚC 1: Khởi tạo body.
        Map<String, Object> body = new HashMap<>();

        // BƯỚC 2: Gắn metadata 400.
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("error", "Bad Request");

        // BƯỚC 3: Đưa message nghiệp vụ ra client.
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Xử lý lỗi xung đột dữ liệu (ví dụ trùng unique).
     *
     * @param ex lỗi ConflictException
     * @return payload 409
     */
    // Trả về 409 khi dữ liệu bị xung đột (trùng khóa, vi phạm uniqueness theo
    // nghiệp vụ).
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflictException(ConflictException ex) {
        // BƯỚC 1: Khởi tạo body.
        Map<String, Object> body = new HashMap<>();

        // BƯỚC 2: Gắn metadata 409.
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 409);
        body.put("error", "Conflict");

        // BƯỚC 3: Trả message cụ thể cho client.
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Xử lý lỗi chưa xác thực hoặc phiên đăng nhập không hợp lệ.
     *
     * @param ex lỗi UnauthorizedException
     * @return payload 401
     */
    // Trả về 401 cho các trường hợp chưa xác thực hoặc phiên không hợp lệ.
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedException(UnauthorizedException ex) {
        // BƯỚC 1: Khởi tạo body.
        Map<String, Object> body = new HashMap<>();

        // BƯỚC 2: Gắn metadata 401.
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 401);
        body.put("error", "Unauthorized");

        // BƯỚC 3: Trả message cho client.
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * Xử lý lỗi không đủ quyền truy cập.
     *
     * @param ex lỗi AccessDeniedException
     * @return payload 403
     */
    // Trả về 403 khi đã xác thực nhưng không đủ quyền.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        // BƯỚC 1: Khởi tạo body.
        Map<String, Object> body = new HashMap<>();

        // BƯỚC 2: Gắn metadata 403.
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 403);
        body.put("error", "Forbidden");

        // BƯỚC 3: Gắn message và trả kết quả.
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /**
     * Xử lý lỗi constraint ở tầng persistence/service.
     *
     * @param ex lỗi ConstraintViolationException
     * @return payload 400 kèm danh sách lỗi dạng chuỗi
     */
    // Chuẩn hóa lỗi validate ở tầng service/repository (constraint violation).
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        // BƯỚC 1: Khởi tạo body.
        Map<String, Object> body = new HashMap<>();

        // BƯỚC 2: Gắn metadata 400.
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("error", "Bad Request");

        // BƯỚC 3: Flatten danh sách violation thành list text để client dễ hiển thị.
        body.put(
                "errors",
                ex.getConstraintViolations()
                        .stream()
                        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                        .toList());

        // BƯỚC 4: Trả response.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Fallback handler cho mọi exception chưa được bắt ở trên.
     *
     * @param ex lỗi bất kỳ
     * @return payload 500
     */
    // Handler cuối cùng để tránh rò rỉ stacktrace ra client nhưng vẫn log đầy đủ
    // cho vận hành.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        // BƯỚC 1: Log lỗi đầy đủ để phục vụ giám sát và điều tra.
        log.error("Unhandled exception caught:", ex);

        // BƯỚC 2: Chuẩn hóa payload phản hồi 500 cho client.
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 500);
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage());

        // BƯỚC 3: Trả response 500.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
