package com.ptithcm.smartshop.repository;

import java.math.BigDecimal;

public interface ProductProjection {
    String getId();
    String getName();
    String getSlug();
    BigDecimal getPrice();
    Boolean getStatus();
    String getCategoryName();
    String getThumbnailUrl();
}
