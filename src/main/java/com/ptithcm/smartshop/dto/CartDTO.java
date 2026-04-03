package com.ptithcm.smartshop.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CartDTO {
    private final List<CartItemDTO> items = new ArrayList<>();

    public List<CartItemDTO> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(CartItemDTO item) {
        if (item == null) {
            return;
        }

        CartItemDTO existing = items.stream()
                .filter(i -> i.getSlug().equals(item.getSlug()))
                .findFirst().orElse(null);

        if (existing == null) {
            items.add(item);
        } else {
            existing.setQuantity(existing.getQuantity() + item.getQuantity());
        }
    }

    public void updateItemQuantity(String slug, int quantity) {
        if (slug == null || slug.isBlank() || quantity < 1) {
            return;
        }

        items.stream()
                .filter(item -> item.getSlug().equals(slug))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));
    }

    public void removeItem(String slug) {
        if (slug == null || slug.isBlank()) {
            return;
        }
        items.removeIf(item -> item.getSlug().equals(slug));
    }

    public void clear() {
        items.clear();
    }

    public int getTotalQuantity() {
        return items.stream().mapToInt(CartItemDTO::getQuantity).sum();
    }

    public BigDecimal getTotalAmount() {
        return items.stream().map(CartItemDTO::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
