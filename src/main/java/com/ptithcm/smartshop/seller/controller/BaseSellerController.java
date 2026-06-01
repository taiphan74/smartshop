package com.ptithcm.smartshop.seller.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

public abstract class BaseSellerController {

    @ModelAttribute
    public void addShopIdToModel(@RequestParam(value = "shopId", required = false) UUID shopId, Model model) {
        if (shopId != null) {
            model.addAttribute("shopId", shopId);
        }
    }
}
