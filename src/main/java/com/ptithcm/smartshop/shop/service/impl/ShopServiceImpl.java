package com.ptithcm.smartshop.shop.service.impl;

import com.ptithcm.smartshop.security.CurrentUserService;
import com.ptithcm.smartshop.shared.exception.ConflictException;
import com.ptithcm.smartshop.shared.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.shared.util.SlugUtil;
import com.ptithcm.smartshop.shop.dto.ShopCreateRequest;
import com.ptithcm.smartshop.shop.dto.ShopResponse;
import com.ptithcm.smartshop.shop.entity.Shop;
import com.ptithcm.smartshop.shop.entity.ShopUser;
import com.ptithcm.smartshop.shop.enums.ShopStatus;
import com.ptithcm.smartshop.shop.enums.ShopUserRole;
import com.ptithcm.smartshop.shop.enums.ShopUserStatus;
import com.ptithcm.smartshop.shop.repository.ShopRepository;
import com.ptithcm.smartshop.shop.repository.ShopUserRepository;
import com.ptithcm.smartshop.shop.service.ShopService;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
/**
 * Triển khai nghiệp vụ quản lý shop.
 *
 * Trách nhiệm chính:
 * - Tạo shop mới cho user hiện tại.
 * - Truy vấn danh sách shop mà user hiện tại sở hữu/tham gia.
 * - Kiểm tra quyền quản lý shop phục vụ nghiệp vụ sản phẩm.
 */
public class ShopServiceImpl implements ShopService {

	private final ShopRepository shopRepository;
	private final ShopUserRepository shopUserRepository;
	private final UserRepository userRepository;
	private final CurrentUserService currentUserService;

	public ShopServiceImpl(
			ShopRepository shopRepository,
			ShopUserRepository shopUserRepository,
			UserRepository userRepository,
			CurrentUserService currentUserService) {
		this.shopRepository = shopRepository;
		this.shopUserRepository = shopUserRepository;
		this.userRepository = userRepository;
		this.currentUserService = currentUserService;
	}

	@Override
	@Transactional
	public ShopResponse create(ShopCreateRequest request) {
		// BƯỚC 1: lấy user hiện tại để gán làm owner cho shop mới.
		UUID currentUserId = currentUserService.getCurrentUserId();
		User owner = userRepository.findById(currentUserId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// BƯỚC 2: resolve slug public của shop và chặn trường hợp trùng.
		String resolvedSlug = resolveShopSlug(request);
		if (shopRepository.existsBySlug(resolvedSlug)) {
			throw new ConflictException("Shop slug already exists");
		}

		// BƯỚC 3: dựng entity shop mới từ dữ liệu request.
		Shop shop = new Shop();
		shop.setOwner(owner);
		shop.setName(request.name().trim());
		shop.setSlug(resolvedSlug);
		shop.setEmail(normalize(request.email()));
		shop.setPhone(normalize(request.phone()));
		shop.setLogoUrl(normalize(request.logoUrl()));
		shop.setBannerUrl(normalize(request.bannerUrl()));
		shop.setDescription(normalize(request.description()));
		shop.setStatus(ShopStatus.ACTIVE);

		// BƯỚC 4: lưu shop trước để lấy id phục vụ tạo membership owner.
		Shop savedShop = shopRepository.save(shop);

		// BƯỚC 5: tạo liên kết owner trong bảng shop_users để đồng bộ membership.
		ShopUser shopUser = new ShopUser();
		shopUser.setShop(savedShop);
		shopUser.setUser(owner);
		shopUser.setShopRole(ShopUserRole.OWNER);
		shopUser.setStatus(ShopUserStatus.ACTIVE);
		shopUserRepository.save(shopUser);

		// BƯỚC 6: trả response sau khi hoàn tất transaction tạo shop.
		return ShopResponse.from(savedShop);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ShopResponse> findMyShops() {
		// BƯỚC 1: xác định user hiện tại.
		UUID currentUserId = currentUserService.getCurrentUserId();
		// BƯỚC 2: lấy các shop user sở hữu hoặc tham gia.
		return shopRepository.findDistinctByOwner_IdOrShopUsers_User_Id(currentUserId, currentUserId).stream()
				.map(ShopResponse::from)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public ShopResponse findBySlug(String slug) {
		// BƯỚC 1: tìm shop theo slug public.
		// BƯỚC 2: map sang response hoặc trả 404 nếu không tồn tại.
		return shopRepository.findBySlug(slug)
				.map(ShopResponse::from)
				.orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
	}

	@Override
	@Transactional(readOnly = true)
	public boolean canManageShop(String shopId) {
		// BƯỚC 1: lấy user hiện tại và parse id shop đầu vào.
		UUID currentUserId = currentUserService.getCurrentUserId();
		UUID parsedShopId = parseUuid(shopId, "shopId");

		// BƯỚC 2: nạp shop để kiểm tra owner trực tiếp.
		Shop shop = shopRepository.findById(parsedShopId)
				.orElseThrow(() -> new ResourceNotFoundException("Shop", shopId));
		if (shop.getOwner() != null && currentUserId.equals(shop.getOwner().getId())) {
			return true;
		}

		// BƯỚC 3: fallback kiểm tra membership active trong shop_users.
		return shopUserRepository.findByShop_IdAndUser_IdAndStatus(parsedShopId, currentUserId, ShopUserStatus.ACTIVE)
				.isPresent();
	}

	/**
	 * Resolve slug public cho shop từ request đầu vào.
	 */
	private String resolveShopSlug(ShopCreateRequest request) {
		String rawSlug = StringUtils.hasText(request.slug()) ? request.slug().trim() : request.name();
		String resolvedSlug = SlugUtil.toSlug(rawSlug);
		if (!StringUtils.hasText(resolvedSlug)) {
			throw new ConflictException("Shop slug is invalid");
		}
		return resolvedSlug;
	}

	/**
	 * Chuẩn hóa chuỗi tùy chọn: trim hoặc trả null nếu rỗng.
	 */
	private String normalize(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}

	/**
	 * Parse UUID và trả lỗi nghiệp vụ nếu định dạng không hợp lệ.
	 */
	private UUID parseUuid(String value, String field) {
		try {
			return UUID.fromString(value);
		} catch (Exception ex) {
			throw new ResourceNotFoundException("Invalid " + field + ": " + value);
		}
	}
}
