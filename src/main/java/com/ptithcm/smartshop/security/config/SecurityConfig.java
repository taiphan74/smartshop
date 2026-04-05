package com.ptithcm.smartshop.security.config;

import com.ptithcm.smartshop.security.principal.CustomUserDetails;
import com.ptithcm.smartshop.security.web.RestAccessDeniedHandler;
import com.ptithcm.smartshop.security.web.RestAuthenticationEntryPoint;
import com.ptithcm.smartshop.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableAspectJAutoProxy
@EnableMethodSecurity
/**
 * Cấu hình bảo mật trung tâm cho ứng dụng.
 *
 * Vai trò chính:
 * - Khai báo SecurityFilterChain và policy xác thực/ủy quyền.
 * - Khai báo UserDetailsService để nạp user từ database.
 * - Khai báo AuthenticationManager, AuthenticationProvider, PasswordEncoder.
 */
public class SecurityConfig {

	/**
	 * Cấu hình chuỗi filter bảo mật cho HTTP request.
	 *
	 * @param http                         đối tượng cấu hình HttpSecurity
	 * @param restAuthenticationEntryPoint handler 401 tùy biến
	 * @param restAccessDeniedHandler      handler 403 tùy biến
	 * @return SecurityFilterChain đã cấu hình
	 * @throws Exception khi cấu hình filter chain thất bại
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			RestAuthenticationEntryPoint restAuthenticationEntryPoint,
			RestAccessDeniedHandler restAccessDeniedHandler) throws Exception {
		// BƯỚC 1: Khởi tạo rule bảo mật cho toàn bộ request.
		// Thiết lập rule bảo mật cho toàn bộ request.
		http
				// BƯỚC 2: Tắt CSRF cho luồng hiện tại (đang dùng session + custom flow).
				.csrf(csrf -> csrf.disable())
				// BƯỚC 3: Gắn handler tùy biến cho lỗi chưa đăng nhập/không đủ quyền.
				// Dùng handler tùy biến để trả response rõ ràng cho 401/403.
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(restAuthenticationEntryPoint)
						.accessDeniedHandler(restAccessDeniedHandler))
				// BƯỚC 4: Định nghĩa whitelist và yêu cầu xác thực cho endpoint còn lại.
				// Mở public cho auth/static, các endpoint còn lại bắt buộc đăng nhập.
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/auth/**", "/css/**", "/error").permitAll()
						.anyRequest().authenticated())
				// BƯỚC 5: Chọn chính sách session cho web MVC.
				// Dùng session cho luồng web MVC (không stateless JWT).
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

		// BƯỚC 6: Build và trả về filter chain hoàn chỉnh.
		return http.build();
	}

	/**
	 * UserDetailsService dùng để truy vấn user khi Spring Security xác thực đăng
	 * nhập.
	 *
	 * @param userRepository repository truy vấn user + roles/permissions
	 * @return implementation UserDetailsService
	 */
	@Bean
	public UserDetailsService userDetailsService(UserRepository userRepository) {
		// BƯỚC 1: Tìm user theo email hoặc số điện thoại.
		// BƯỚC 2: Map sang CustomUserDetails để Security sử dụng.
		// BƯỚC 3: Nếu không có thì ném UsernameNotFoundException.
		// Nạp user + role/permission để Spring Security xây dựng Authentication.
		return username -> userRepository.findByEmailOrPhoneWithRoles(username)
				.map(CustomUserDetails::from)
				.orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
						"Invalid credentials"));
	}

	/**
	 * Cấu hình provider xác thực dùng UserDetailsService + PasswordEncoder.
	 *
	 * @param userDetailsService service nạp thông tin người dùng
	 * @param passwordEncoder    bộ mã hóa mật khẩu
	 * @return DaoAuthenticationProvider đã cấu hình
	 */
	@Bean
	public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		// BƯỚC 1: Tạo provider.
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
		// BƯỚC 2: Gắn password encoder để đối chiếu mật khẩu đã hash.
		authenticationProvider.setPasswordEncoder(passwordEncoder);
		// BƯỚC 3: Trả provider về context.
		return authenticationProvider;
	}

	/**
	 * Lấy AuthenticationManager từ AuthenticationConfiguration của Spring.
	 *
	 * @param authenticationConfiguration cấu hình authentication hiện tại
	 * @return AuthenticationManager
	 * @throws Exception khi không thể khởi tạo manager
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		// BƯỚC 1: Truy xuất manager đã được Spring lắp ghép từ provider chain.
		return authenticationConfiguration.getAuthenticationManager();
	}

	/**
	 * Khai báo password encoder chuẩn BCrypt.
	 *
	 * @return PasswordEncoder dùng BCrypt
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		// BƯỚC 1: Trả về implementation BCrypt để hash/check mật khẩu.
		return new BCryptPasswordEncoder();
	}
}
