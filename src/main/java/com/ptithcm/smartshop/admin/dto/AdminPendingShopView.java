package com.ptithcm.smartshop.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record AdminPendingShopView(UUID id, String name, String ownerName, Instant createdAt) {
}
