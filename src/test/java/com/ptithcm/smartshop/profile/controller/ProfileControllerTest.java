package com.ptithcm.smartshop.profile.controller;

import com.ptithcm.smartshop.profile.dto.ProfileUpdateForm;
import com.ptithcm.smartshop.profile.dto.ShopRegistrationForm;
import com.ptithcm.smartshop.security.session.SessionUser;
import com.ptithcm.smartshop.shop.dto.ShopSummary;
import com.ptithcm.smartshop.shop.enums.ShopStatus;
import com.ptithcm.smartshop.shop.service.ShopRegistrationService;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileControllerTest {

    @Test
    void profilePageRedirectsGuestToLogin() {
        ProfileController controller = new ProfileController(mock(UserRepository.class), mock(ShopRegistrationService.class));

        String view = controller.profile(null, new ConcurrentModel());

        assertThat(view).isEqualTo("redirect:/auth/login");
    }

    @Test
    void profilePageShowsCurrentUserAndShops() {
        UUID userId = UUID.randomUUID();
        SessionUser sessionUser = new SessionUser(userId, "seller@example.com", "0900000000", "Nguyen Van A", Set.of("CUSTOMER"));
        User user = new User();
        user.setId(userId);
        user.setEmail("seller@example.com");
        user.setPhone("0900000000");
        UserRepository userRepository = mock(UserRepository.class);
        ShopRegistrationService shopService = mock(ShopRegistrationService.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(shopService.findOwnedShopSummaries(userId)).thenReturn(List.of(new ShopSummary("Shop A", "shop-a", "", "0911111111", ShopStatus.PENDING)));
        ProfileController controller = new ProfileController(userRepository, shopService);
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.profile(sessionUser, model);

        assertThat(view).isEqualTo("profile/index");
        assertThat(model.getAttribute("profileForm")).isInstanceOf(ProfileUpdateForm.class);
        assertThat(model.getAttribute("shopForm")).isInstanceOf(ShopRegistrationForm.class);
        assertThat((List<?>) model.getAttribute("shops")).hasSize(1);
    }

    @Test
    void updateProfileChangesOnlyEditableFields() {
        UUID userId = UUID.randomUUID();
        SessionUser sessionUser = new SessionUser(userId, "seller@example.com", "0900000000", "Old Name", Set.of("CUSTOMER"));
        User user = new User();
        user.setId(userId);
        user.setEmail("seller@example.com");
        user.setPhone("0900000000");
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        ProfileController controller = new ProfileController(userRepository, mock(ShopRegistrationService.class));

        String view = controller.updateProfile(sessionUser, new ProfileUpdateForm("New Name", "0911111111"), new RedirectAttributesModelMap());

        assertThat(view).isEqualTo("redirect:/profile");
        assertThat(user.getPhone()).isEqualTo("0911111111");
        assertThat(user.getEmail()).isEqualTo("seller@example.com");
        assertThat(user.getFullName()).isEqualTo("New Name");
        verify(userRepository).save(user);
    }

    @Test
    void registerShopDelegatesToShopServiceForCurrentUser() {
        UUID userId = UUID.randomUUID();
        SessionUser sessionUser = new SessionUser(userId, "seller@example.com", "0900000000", "Nguyen Van A", Set.of("CUSTOMER"));
        ShopRegistrationService shopService = mock(ShopRegistrationService.class);
        ProfileController controller = new ProfileController(mock(UserRepository.class), shopService);
        ShopRegistrationForm form = new ShopRegistrationForm("Shop A", "Desc", "0911111111", "Address");

        String view = controller.registerShop(sessionUser, form, new RedirectAttributesModelMap());

        assertThat(view).isEqualTo("redirect:/profile");
        verify(shopService).register(eq(userId), any(ShopRegistrationForm.class));
    }
}
