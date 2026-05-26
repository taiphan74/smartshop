package com.ptithcm.smartshop.seller.service;

import java.util.Map;
import java.util.UUID;

public interface SellerDashboardService {
    Map<String, Object> getDashboardMetrics(UUID shopId);
}
