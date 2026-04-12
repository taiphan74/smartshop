package com.ptithcm.smartshop.shop.service;

import com.ptithcm.smartshop.shop.dto.ShopCreateRequest;
import com.ptithcm.smartshop.shop.dto.ShopResponse;
import java.util.List;

public interface ShopService {

	ShopResponse create(ShopCreateRequest request);

	List<ShopResponse> findMyShops();

	ShopResponse findBySlug(String slug);

	boolean canManageShop(String shopId);
}
