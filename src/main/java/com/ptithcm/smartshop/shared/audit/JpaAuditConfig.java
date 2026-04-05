package com.ptithcm.smartshop.shared.audit;

import com.ptithcm.smartshop.security.principal.CustomUserDetails;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
/**
 * Cấu hình audit cho JPA.
 * Mục tiêu: tự động điền giá trị cho createdBy/updatedBy trong các entity dùng
 * AuditableEntity.
 */
public class JpaAuditConfig {

    /**
     * Tạo bean AuditorAware để Spring Data JPA biết "ai" là người thực hiện thao
     * tác ghi dữ liệu.
     *
     * @return AuditorAware chứa định danh người dùng hiện tại.
     */
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            // BƯỚC 1: Đọc Authentication từ SecurityContext hiện tại.
            // Lấy thông tin xác thực hiện tại để xác định ai đang thực hiện thay đổi dữ
            // liệu.
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // BƯỚC 2: Nếu không có đăng nhập hợp lệ thì gán giá trị mặc định "system".
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication instanceof AnonymousAuthenticationToken) {
                // Không có phiên đăng nhập hợp lệ thì dùng tài khoản hệ thống.
                return Optional.of("system");
            }

            // BƯỚC 3: Lấy principal và cố gắng map theo các kiểu principal hỗ trợ.
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails customUserDetails) {
                // Ưu tiên lưu UUID người dùng nội bộ để truy vết chính xác theo domain.
                if (customUserDetails.getId() != null) {
                    return Optional.of(customUserDetails.getId().toString());
                }
                return Optional.of("system");
            }

            // BƯỚC 4: Fallback với UserDetails (trường hợp provider không dùng
            // CustomUserDetails).
            if (principal instanceof UserDetails userDetails) {
                // Fallback cho các luồng chỉ có username (ví dụ provider khác).
                String username = userDetails.getUsername();
                if (username != null && !username.isBlank()) {
                    return Optional.of(username);
                }
                return Optional.of("system");
            }

            // BƯỚC 5: Fallback với principal dạng chuỗi đã xác thực.
            if (principal instanceof String principalName
                    && !principalName.isBlank()
                    && !"anonymousUser".equalsIgnoreCase(principalName)) {
                // Trường hợp principal là chuỗi đã xác thực.
                return Optional.of(principalName);
            }

            // BƯỚC 6: Chốt giá trị cuối cùng để không bao giờ trả Optional rỗng.
            // Chốt fallback an toàn để luôn có giá trị cho createdBy/updatedBy.
            return Optional.of("system");
        };
    }
}
