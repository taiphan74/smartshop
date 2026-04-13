package com.ptithcm.smartshop.banner.repository;

import com.ptithcm.smartshop.banner.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BannerRepository extends JpaRepository<Banner, UUID> {

    List<Banner> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<Banner> findAllByOrderByDisplayOrderAsc();
}
