package com.ptithcm.smartshop.auth.service.impl;

import com.ptithcm.smartshop.auth.dto.AuthLoginRequest;
import com.ptithcm.smartshop.auth.dto.AuthRegisterRequest;
import com.ptithcm.smartshop.auth.dto.AuthResponse;
import com.ptithcm.smartshop.auth.service.AuthService;
import com.ptithcm.smartshop.shared.exception.ConflictException;
import com.ptithcm.smartshop.shared.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.shared.exception.UnauthorizedException;
import com.ptithcm.smartshop.security.principal.CustomUserDetails;
import com.ptithcm.smartshop.security.session.SessionConstants;
import com.ptithcm.smartshop.security.session.SessionUser;
import com.ptithcm.smartshop.security.rbac.repository.RoleRepository;
import com.ptithcm.smartshop.user.dto.UserResponse;
import com.ptithcm.smartshop.user.entity.Role;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.enums.UserStatus;
import com.ptithcm.smartshop.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Set;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Triển khai nghiệp vụ xác thực người dùng.
 *
 * Trách nhiệm chính:
 * - Đăng ký tài khoản mới với role mặc định CUSTOMER.
 * - Đăng nhập, thiết lập SecurityContext và SessionUser vào HTTP session.
 * - Đăng xuất, hủy session và dọn security context.
 * - Truy xuất thông tin phiên hiện tại.
 */
@Service
public class AuthServiceImpl implements AuthService {

	/**
	 * Mã role mặc định áp dụng cho người dùng tự đăng ký.
	 */
	private static final String CUSTOMER_ROLE_CODE = "CUSTOMER";

	/**
	 * Repository truy cập dữ liệu người dùng.
	 */
	private final UserRepository userRepository;
	/**
	 * Repository truy cập dữ liệu role/phân quyền.
	 */
	private final RoleRepository roleRepository;
	/**
	 * Thành phần mã hóa mật khẩu trước khi lưu DB.
	 */
	private final PasswordEncoder passwordEncoder;
	/**
	 * Thành phần xác thực username/password chuẩn Spring Security.
	 */
	private final AuthenticationManager authenticationManager;

	/**
	 * Khởi tạo service với các dependency bắt buộc.
	 */
	public AuthServiceImpl(
			UserRepository userRepository,
			RoleRepository roleRepository,
			PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
	}

	@Override
	@Transactional
	public AuthResponse register(AuthRegisterRequest request) {
		// Bước 1: kiểm tra trùng email để đảm bảo tính duy nhất.
		if (userRepository.existsByEmail(request.email())) {
			throw new ConflictException("Email already exists");
		}
		// Bước 2: kiểm tra trùng số điện thoại để đảm bảo tính duy nhất.
		if (userRepository.existsByPhone(request.phone())) {
			throw new ConflictException("Phone already exists");
		}

		// Bước 3: lấy role mặc định CUSTOMER; nếu thiếu cấu hình thì trả lỗi nghiệp vụ.
		Role customerRole = roleRepository.findByCode(CUSTOMER_ROLE_CODE)
				.orElseThrow(() -> new ResourceNotFoundException("Default role CUSTOMER was not found"));

		// Bước 4: dựng entity User mới từ dữ liệu request.
		User user = new User();
		user.setEmail(request.email());
		user.setPhone(request.phone());
		// Bước 5: mã hóa mật khẩu trước khi lưu để bảo mật thông tin xác thực.
		user.setPassword(passwordEncoder.encode(request.password()));
		user.setFullName(request.fullName());
		// Bước 6: kích hoạt trạng thái tài khoản mặc định và gán role customer.
		user.setStatus(UserStatus.ACTIVE);
		user.setRoles(Set.of(customerRole));

		// Bước 7: lưu user trong transaction để đảm bảo tính toàn vẹn dữ liệu.
		User savedUser = userRepository.save(user);
		// Bước 8: trả response thành công, chưa có session vì mới đăng ký.
		return new AuthResponse("Register successful", null, null, UserResponse.from(savedUser));
	}

	@Override
	@Transactional(readOnly = true)
	public AuthResponse login(AuthLoginRequest request, HttpServletRequest httpServletRequest) {
		// Bước 1: xác thực thông tin đăng nhập (email/phone + password).
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.emailOrPhone(), request.password()));

		// Bước 2: lấy principal sau xác thực để truy xuất ID người dùng.
		CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
		// Bước 3: nạp lại entity user đầy đủ từ DB phục vụ mapping response/session.
		User user = userRepository.findById(principal.getId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// Bước 4: tạo security context mới và gắn authentication hiện tại.
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		// Bước 5: set context vào SecurityContextHolder cho request thread hiện tại.
		SecurityContextHolder.setContext(context);

		// Bước 6: tạo/lấy HTTP session để lưu trạng thái đăng nhập xuyên request.
		HttpSession session = httpServletRequest.getSession(true);
		// Bước 7: lưu SecurityContext chuẩn Spring Security vào session.
		session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
		// Bước 8: tạo snapshot SessionUser rút gọn để dùng cho UI/logic nhanh.
		SessionUser sessionUser = SessionUser.from(user);
		// Bước 9: lưu SessionUser vào session với key chuẩn nội bộ.
		session.setAttribute(SessionConstants.CURRENT_USER, sessionUser);

		// Bước 10: trả thông tin đăng nhập thành công kèm session id.
		return new AuthResponse("Login successful", session.getId(), sessionUser, UserResponse.from(user));
	}

	@Override
	public void logout(HttpServletRequest httpServletRequest) {
		// Bước 1: lấy session hiện tại, không tạo mới khi người dùng chưa có session.
		HttpSession session = httpServletRequest.getSession(false);
		if (session != null) {
			// Bước 2: vô hiệu hóa session để xóa toàn bộ dữ liệu đăng nhập.
			session.invalidate();
		}
		// Bước 3: dọn SecurityContext ở thread hiện tại.
		SecurityContextHolder.clearContext();
	}

	@Override
	@Transactional(readOnly = true)
	public AuthResponse me(HttpServletRequest httpServletRequest) {
		// Bước 1: lấy session hiện hữu; nếu không có thì coi như chưa đăng nhập.
		HttpSession session = httpServletRequest.getSession(false);
		if (session == null) {
			throw new UnauthorizedException("No active session");
		}

		// Bước 2: đọc SessionUser từ session để xác thực trạng thái đăng nhập.
		SessionUser sessionUser = (SessionUser) session.getAttribute(SessionConstants.CURRENT_USER);
		if (sessionUser == null) {
			throw new UnauthorizedException("No active session");
		}

		// Bước 3: nạp user mới nhất từ DB để đảm bảo dữ liệu trả về cập nhật.
		User user = userRepository.findById(sessionUser.id())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// Bước 4: trả thông tin phiên hiện tại cho client/UI.
		return new AuthResponse("Current session", session.getId(), sessionUser, UserResponse.from(user));
	}
}
