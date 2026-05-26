package com.ptithcm.smartshop.shop.service;

import com.ptithcm.smartshop.profile.dto.ShopRegistrationForm;
import com.ptithcm.smartshop.shop.entity.Shop;
import com.ptithcm.smartshop.shop.enums.ShopStatus;
import com.ptithcm.smartshop.shop.repository.ShopRepository;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShopRegistrationServiceTest {

    @Test
    void registerCreatesPendingShopForCurrentUser() {
        UUID userId = UUID.randomUUID();
        User owner = new User();
        owner.setId(userId);
        owner.setEmail("seller@example.com");
        owner.setPhone("0900000000");
        UserRepository userRepository = mock(UserRepository.class);
        ShopRepository shopRepository = mock(ShopRepository.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(shopRepository.existsBySlug("nha-sach-xanh")).thenReturn(false);
        when(shopRepository.save(any(Shop.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ShopRegistrationService service = new ShopRegistrationService(shopRepository, userRepository);
        ShopRegistrationForm form = new ShopRegistrationForm("Nhà Sách Xanh", "Sách chọn lọc", "0911111111", "12 Nguyễn Trãi");

        Shop shop = service.register(userId, form);

        assertThat(shop.getOwner()).isSameAs(owner);
        assertThat(shop.getName()).isEqualTo("Nhà Sách Xanh");
        assertThat(shop.getSlug()).isEqualTo("nha-sach-xanh");
        assertThat(shop.getDescription()).isEqualTo("Sách chọn lọc");
        assertThat(shop.getPhone()).isEqualTo("0911111111");
        assertThat(shop.getStatus()).isEqualTo(ShopStatus.PENDING);
    }

    @Test
    void registerAddsNumericSuffixWhenSlugAlreadyExists() {
        UUID userId = UUID.randomUUID();
        User owner = new User();
        owner.setId(userId);
        UserRepository userRepository = mock(UserRepository.class);
        ShopRepository shopRepository = mock(ShopRepository.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(shopRepository.existsBySlug("shop-demo")).thenReturn(true);
        when(shopRepository.existsBySlug("shop-demo-2")).thenReturn(true);
        when(shopRepository.existsBySlug("shop-demo-3")).thenReturn(false);
        when(shopRepository.save(any(Shop.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ShopRegistrationService service = new ShopRegistrationService(shopRepository, userRepository);

        Shop shop = service.register(userId, new ShopRegistrationForm("Shop Demo", "", "", ""));

        assertThat(shop.getSlug()).isEqualTo("shop-demo-3");
    }

    @Test
    void findOwnedShopSummariesReturnsCurrentUsersShops() {
        UUID userId = UUID.randomUUID();
        User owner = new User();
        owner.setId(userId);
        Shop shop = new Shop();
        shop.setName("Shop A");
        shop.setSlug("shop-a");
        shop.setStatus(ShopStatus.PENDING);
        shop.setDescription("Pending shop");
        shop.setId(UUID.randomUUID());
        ShopRepository shopRepository = mock(ShopRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        when(shopRepository.findByOwnerIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(shop));
        ShopRegistrationService service = new ShopRegistrationService(shopRepository, userRepository);

        var shops = service.findOwnedShopSummaries(userId);

        assertThat(shops).hasSize(1);
        assertThat(shops.get(0).name()).isEqualTo("Shop A");
        assertThat(shops.get(0).status()).isEqualTo(ShopStatus.PENDING);
    }
}
