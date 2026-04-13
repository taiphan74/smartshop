# Banner Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a Swiper-powered banner carousel to the homepage with inline admin CRUD controls and file upload.

**Architecture:** New `banner/` package (entity, repository, service, controller) following existing codebase patterns. Homepage integrates Swiper CDN for carousel display. Admin users see inline edit/delete/add controls overlayed on the carousel. File uploads saved to `static/uploads/banners/`.

**Tech Stack:** Spring Boot 4.0.5, Java 21, JPA/Hibernate, Thymeleaf, Swiper 12 (CDN), PostgreSQL, Liquibase

---

## File Map

| File | Action | Responsibility |
|------|--------|----------------|
| `src/main/java/.../banner/entity/Banner.java` | Create | JPA entity extending `AuditableEntity` |
| `src/main/java/.../banner/dto/BannerForm.java` | Create | Form DTO for Thymeleaf binding + validation |
| `src/main/java/.../banner/repository/BannerRepository.java` | Create | Spring Data JPA repository |
| `src/main/java/.../banner/service/BannerService.java` | Create | Service interface |
| `src/main/java/.../banner/service/impl/BannerServiceImpl.java` | Create | Service implementation + file upload logic |
| `src/main/java/.../banner/config/BannerUploadConfig.java` | Create | Upload directory configuration bean |
| `src/main/java/.../banner/controller/AdminBannerController.java` | Create | Thymeleaf controller for admin CRUD |
| `src/main/resources/db/changelog/changes/009-create-banner-table.yaml` | Create | Liquibase migration |
| `src/main/resources/db/changelog/changes/010-seed-banner-permissions.yaml` | Create | Seed banner permissions |
| `src/main/java/.../security/rbac/Permission.java` | Modify | Add BANNER_* enums |
| `src/main/java/.../security/config/SecurityConfig.java` | Modify | Allow `/uploads/**` |
| `src/main/java/.../auth/controller/HomeController.java` | Modify | Inject BannerService, add banners to model |
| `src/main/resources/templates/fragments/head.html` | Modify | Add Swiper CDN CSS |
| `src/main/resources/templates/home.html` | Modify | Add Swiper carousel HTML + JS |
| `.gitignore` | Modify | Ignore `uploads/` |

---

### Task 1: Database Schema — Banner Table

**Files:**
- Create: `src/main/resources/db/changelog/changes/009-create-banner-table.yaml`
- Modify: `src/main/resources/db/changelog/db.changelog-master.yaml`

- [ ] **Step 1: Create the Liquibase migration for the banners table**

```yaml
# src/main/resources/db/changelog/changes/009-create-banner-table.yaml
databaseChangeLog:
  - changeSet:
      id: 009-create-banner-table
      author: claude
      changes:
        - createTable:
            tableName: banners
            columns:
              - column:
                  name: id
                  type: uuid
                  defaultValueComputed: gen_random_uuid()
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: title
                  type: varchar(255)
              - column:
                  name: image_url
                  type: varchar(500)
                  constraints:
                    nullable: false
              - column:
                  name: link_url
                  type: varchar(500)
              - column:
                  name: display_order
                  type: int
                  defaultValue: 0
                  constraints:
                    nullable: false
              - column:
                  name: is_active
                  type: boolean
                  defaultValue: true
                  constraints:
                    nullable: false
              - column:
                  name: created_at
                  type: timestamp
                  defaultValueComputed: now()
                  constraints:
                    nullable: false
              - column:
                  name: updated_at
                  type: timestamp
                  defaultValueComputed: now()
                  constraints:
                    nullable: false
              - column:
                  name: created_by
                  type: varchar(100)
              - column:
                  name: updated_by
                  type: varchar(100)
```

- [ ] **Step 2: Add includeAll to db.changelog-master.yaml**

Read `src/main/resources/db/changelog/db.changelog-master.yaml` and verify it uses `includeAll`. If it does, no change needed. If it uses individual includes, add:

```yaml
  - include:
      file: db/changelog/changes/009-create-banner-table.yaml
```

- [ ] **Step 3: Run the application to verify migration**

```bash
docker compose up -d
./mvnw spring-boot:run
```

Wait for startup and check logs for "Liquibase: Update successful". Ctrl+C after confirming.

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/db/changelog/
git commit -m "feat: add banner table migration"
```

---

### Task 2: Banner Entity + Repository

**Files:**
- Create: `src/main/java/com/ptithcm/smartshop/banner/entity/Banner.java`
- Create: `src/main/java/com/ptithcm/smartshop/banner/repository/BannerRepository.java`

**Pattern reference:** Banner extends `AuditableEntity` (from `com.ptithcm.smartshop.shared.entity.AuditableEntity`) which provides `id` (UUID), `createdAt`, `updatedAt`, `createdBy`, `updatedBy`. Follows same style as `Product`, `User`, etc.

- [ ] **Step 1: Create the Banner entity**

```java
package com.ptithcm.smartshop.banner.entity;

import com.ptithcm.smartshop.shared.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "banners")
public class Banner extends AuditableEntity {

    @Column(length = 255)
    private String title;

    @Column(name = "image_url", length = 500, nullable = false)
    private String imageUrl;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @PrePersist
    protected void onPrePersist() {
        if (this.displayOrder == null) {
            this.displayOrder = 0;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    // Getters and Setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
```

- [ ] **Step 2: Create the BannerRepository**

```java
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
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/ptithcm/smartshop/banner/
git commit -m "feat: add Banner entity and repository"
```

---

### Task 3: Banner Service + DTO

**Files:**
- Create: `src/main/java/com/ptithcm/smartshop/banner/dto/BannerForm.java`
- Create: `src/main/java/com/ptithcm/smartshop/banner/service/BannerService.java`
- Create: `src/main/java/com/ptithcm/smartshop/banner/service/impl/BannerServiceImpl.java`
- Create: `src/main/java/com/ptithcm/smartshop/banner/config/BannerUploadConfig.java`

- [ ] **Step 1: Create BannerForm DTO**

```java
package com.ptithcm.smartshop.banner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record BannerForm(
        String title,

        @NotBlank(message = "Image is required")
        String imageUrl,

        String linkUrl,

        @NotNull(message = "Display order is required")
        Integer displayOrder,

        Boolean isActive
) {
    public BannerForm {
        if (displayOrder == null) displayOrder = 0;
        if (isActive == null) isActive = true;
    }
}
```

- [ ] **Step 2: Create BannerService interface**

```java
package com.ptithcm.smartshop.banner.service;

import com.ptithcm.smartshop.banner.dto.BannerForm;
import com.ptithcm.smartshop.banner.entity.Banner;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface BannerService {

    List<Banner> findAllActive();

    List<Banner> findAll();

    Banner findById(UUID id);

    Banner create(BannerForm form, MultipartFile imageFile);

    Banner update(UUID id, BannerForm form, MultipartFile imageFile);

    void delete(UUID id);
}
```

- [ ] **Step 3: Create BannerUploadConfig**

```java
package com.ptithcm.smartshop.banner.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class BannerUploadConfig {

    @Value("${banner.upload.dir:src/main/resources/static/uploads/banners}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir);
        try {
            Files.createDirectories(uploadPath);
        } catch (Exception e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
    }

    public Path getUploadPath() {
        return uploadPath;
    }
}
```

- [ ] **Step 4: Create BannerServiceImpl**

```java
package com.ptithcm.smartshop.banner.service.impl;

import com.ptithcm.smartshop.banner.config.BannerUploadConfig;
import com.ptithcm.smartshop.banner.dto.BannerForm;
import com.ptithcm.smartshop.banner.entity.Banner;
import com.ptithcm.smartshop.banner.repository.BannerRepository;
import com.ptithcm.smartshop.banner.service.BannerService;
import com.ptithcm.smartshop.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

@Service
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;
    private final BannerUploadConfig uploadConfig;

    public BannerServiceImpl(BannerRepository bannerRepository, BannerUploadConfig uploadConfig) {
        this.bannerRepository = bannerRepository;
        this.uploadConfig = uploadConfig;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Banner> findAllActive() {
        return bannerRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Banner> findAll() {
        return bannerRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Banner findById(UUID id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner", id.toString()));
    }

    @Override
    @Transactional
    public Banner create(BannerForm form, MultipartFile imageFile) {
        Banner banner = new Banner();
        banner.setTitle(form.title());
        banner.setLinkUrl(form.linkUrl());
        banner.setDisplayOrder(form.displayOrder());
        banner.setIsActive(form.isActive());

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = saveImage(imageFile);
            banner.setImageUrl(imageUrl);
        } else {
            banner.setImageUrl(form.imageUrl());
        }

        return bannerRepository.save(banner);
    }

    @Override
    @Transactional
    public Banner update(UUID id, BannerForm form, MultipartFile imageFile) {
        Banner banner = findById(id);

        banner.setTitle(form.title());
        banner.setLinkUrl(form.linkUrl());
        banner.setDisplayOrder(form.displayOrder());
        banner.setIsActive(form.isActive());

        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete old image if it exists
            deleteImage(banner.getImageUrl());
            String imageUrl = saveImage(imageFile);
            banner.setImageUrl(imageUrl);
        } else if (form.imageUrl() != null && !form.imageUrl().isBlank()) {
            banner.setImageUrl(form.imageUrl());
        }

        return bannerRepository.save(banner);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Banner banner = findById(id);
        deleteImage(banner.getImageUrl());
        bannerRepository.delete(banner);
    }

    private String saveImage(MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;

        try {
            Files.copy(file.getInputStream(), uploadConfig.getUploadPath().resolve(fileName));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + fileName, e);
        }

        return "/uploads/banners/" + fileName;
    }

    private void deleteImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/banners/")) {
            return;
        }
        String fileName = imageUrl.substring("/uploads/banners/".length());
        try {
            Files.deleteIfExists(uploadConfig.getUploadPath().resolve(fileName));
        } catch (IOException e) {
            // Log but don't fail the delete operation
        }
    }

    private String getFileExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".jpg";
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/ptithcm/smartshop/banner/
git commit -m "feat: add banner service, DTO, and upload config"
```

---

### Task 4: Admin Banner Controller

**Files:**
- Create: `src/main/java/com/ptithcm/smartshop/banner/controller/AdminBannerController.java`
- Modify: `src/main/java/com/ptithcm/smartshop/security/rbac/Permission.java`
- Create: `src/main/resources/db/changelog/changes/010-seed-banner-permissions.yaml`

- [ ] **Step 1: Add BANNER permissions to Permission enum**

```java
// src/main/java/com/ptithcm/smartshop/security/rbac/Permission.java
// Add these entries to the existing enum:
BANNER_READ("banner:read"),
BANNER_CREATE("banner:create"),
BANNER_UPDATE("banner:update"),
BANNER_DELETE("banner:delete");
```

The full file becomes:

```java
package com.ptithcm.smartshop.security.rbac;

public enum Permission {
    USER_READ("user:read"),
    USER_CREATE("user:create"),
    USER_UPDATE("user:update"),
    USER_DELETE("user:delete"),
    BANNER_READ("banner:read"),
    BANNER_CREATE("banner:create"),
    BANNER_UPDATE("banner:update"),
    BANNER_DELETE("banner:delete");

    private final String code;

    Permission(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
```

- [ ] **Step 2: Create banner permissions seed migration**

```yaml
# src/main/resources/db/changelog/changes/010-seed-banner-permissions.yaml
databaseChangeLog:
  - changeSet:
      id: 010-seed-banner-permissions
      author: claude
      changes:
        - insert:
            tableName: permissions
            columns:
              - column: { name: code, value: "banner:read" }
              - column: { name: description, value: "View banners" }
        - insert:
            tableName: permissions
            columns:
              - column: { name: code, value: "banner:create" }
              - column: { name: description, value: "Create new banners" }
        - insert:
            tableName: permissions
            columns:
              - column: { name: code, value: "banner:update" }
              - column: { name: description, value: "Update existing banners" }
        - insert:
            tableName: permissions
            columns:
              - column: { name: code, value: "banner:delete" }
              - column: { name: description, value: "Delete banners" }
        - sql:
            sql: |
              INSERT INTO role_permissions (role_id, permission_id)
              SELECT r.id, p.id
              FROM roles r
              CROSS JOIN permissions p
              WHERE r.name = 'ADMIN'
                AND p.code IN ('banner:read', 'banner:create', 'banner:update', 'banner:delete')
                AND NOT EXISTS (
                  SELECT 1 FROM role_permissions rp
                  WHERE rp.role_id = r.id AND rp.permission_id = p.id
                );
```

- [ ] **Step 3: Create AdminBannerController**

```java
package com.ptithcm.smartshop.banner.controller;

import com.ptithcm.smartshop.banner.dto.BannerForm;
import com.ptithcm.smartshop.banner.entity.Banner;
import com.ptithcm.smartshop.banner.service.BannerService;
import com.ptithcm.smartshop.security.rbac.Permission;
import com.ptithcm.smartshop.security.rbac.RequirePermission;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/banners")
public class AdminBannerController {

    private final BannerService bannerService;

    public AdminBannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    @RequirePermission(Permission.BANNER_READ)
    public String list(Model model) {
        List<Banner> banners = bannerService.findAll();
        model.addAttribute("banners", banners);
        return "admin/banners/list";
    }

    @PostMapping("/create")
    @RequirePermission(Permission.BANNER_CREATE)
    public String create(
            @ModelAttribute("bannerForm") @Valid BannerForm form,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model) {

        if (result.hasErrors() || (imageFile == null || imageFile.isEmpty()) && (form.imageUrl() == null || form.imageUrl().isBlank())) {
            model.addAttribute("banners", bannerService.findAll());
            model.addAttribute("editing", false);
            return "admin/banners/list";
        }

        bannerService.create(form, imageFile);
        return "redirect:/admin/banners";
    }

    @PostMapping("/update/{id}")
    @RequirePermission(Permission.BANNER_UPDATE)
    public String update(
            @PathVariable UUID id,
            @ModelAttribute("bannerForm") @Valid BannerForm form,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("banners", bannerService.findAll());
            model.addAttribute("editingId", id);
            return "admin/banners/list";
        }

        bannerService.update(id, form, imageFile);
        return "redirect:/admin/banners";
    }

    @PostMapping("/delete/{id}")
    @RequirePermission(Permission.BANNER_DELETE)
    public String delete(@PathVariable UUID id) {
        bannerService.delete(id);
        return "redirect:/admin/banners";
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/ptithcm/smartshop/security/rbac/Permission.java \
          src/main/java/com/ptithcm/smartshop/banner/controller/ \
          src/main/resources/db/changelog/changes/010-seed-banner-permissions.yaml
git commit -m "feat: add admin banner controller and permissions"
```

---

### Task 5: Admin Banner List Template

**Files:**
- Create: `src/main/resources/templates/admin/banners/list.html`
- Modify: `src/main/java/com/ptithcm/smartshop/security/config/SecurityConfig.java`

- [ ] **Step 1: Allow `/uploads/**` in SecurityConfig**

Modify the `authorizeHttpRequests` section in `SecurityConfig.java`:

```java
.requestMatchers(
    "/",
    "/products/**",
    "/auth/login",
    "/auth/register",
    "/css/**",
    "/js/**",
    "/images/**",
    "/webjars/**",
    "/uploads/**"
).permitAll()
```

- [ ] **Step 2: Create the admin banner list template**

```html
<!-- src/main/resources/templates/admin/banners/list.html -->
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/head :: head('Quản lý Banner')}"></head>
<body>

<div th:replace="~{fragments/header :: header}"></div>

<main class="dashboard-shell">
    <div class="container mx-auto py-8 px-6">
        <h1 class="text-2xl font-bold text-gray-800 mb-6">Quản lý Banner</h1>

        <!-- Create Form -->
        <div class="bg-white rounded-lg shadow p-6 mb-6">
            <h2 class="text-lg font-semibold mb-4">Thêm Banner mới</h2>
            <form th:action="@{/admin/banners/create}" method="post" enctype="multipart/form-data"
                  th:object="${bannerForm}" class="space-y-4">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700">Tiêu đề</label>
                        <input type="text" th:field="*{title}" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm"/>
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700">URL đích</label>
                        <input type="text" th:field="*{linkUrl}" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm"/>
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700">Thứ tự</label>
                        <input type="number" th:field="*{displayOrder}" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm"/>
                    </div>
                    <div class="flex items-center mt-6">
                        <input type="checkbox" th:field="*{isActive}" class="mr-2"/>
                        <label class="text-sm text-gray-700">Hiển thị</label>
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700">Hình ảnh</label>
                        <input type="file" name="imageFile" accept="image/*" class="mt-1 block w-full"/>
                    </div>
                </div>
                <button type="submit" class="bg-[#0A68FF] text-white px-6 py-2 rounded-lg hover:bg-blue-700">
                    Thêm banner
                </button>
            </form>
        </div>

        <!-- Banner List -->
        <div class="space-y-4">
            <div th:each="banner : ${banners}" class="bg-white rounded-lg shadow p-4 flex items-center gap-4">
                <img th:src="${banner.imageUrl}" alt="Banner" class="w-48 h-24 object-cover rounded"/>
                <div class="flex-1">
                    <h3 class="font-medium" th:text="${banner.title}">Banner Title</h3>
                    <p class="text-sm text-gray-500" th:text="'Thứ tự: ' + ${banner.displayOrder}"></p>
                    <span th:if="${banner.isActive}" class="text-xs text-green-600">Đang hiển thị</span>
                    <span th:unless="${banner.isActive}" class="text-xs text-red-600">Đã ẩn</span>
                </div>
                <form th:action="@{'/admin/banners/delete/' + ${banner.id}}" method="post"
                      onsubmit="return confirm('Bạn có chắc muốn xóa banner này?');">
                    <button type="submit" class="text-red-600 hover:text-red-800 text-sm">Xóa</button>
                </form>
            </div>
        </div>

        <a href="/" class="mt-6 inline-block text-[#0A68FF] hover:underline">&larr; Quay lại trang chủ</a>
    </div>
</main>

</body>
</html>
```

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/templates/admin/ \
          src/main/java/com/ptithcm/smartshop/security/config/SecurityConfig.java
git commit -m "feat: add admin banner list template and security config"
```

---

### Task 6: Homepage Swiper Integration

**Files:**
- Modify: `src/main/resources/templates/fragments/head.html`
- Modify: `src/main/java/com/ptithcm/smartshop/auth/controller/HomeController.java`
- Modify: `src/main/resources/templates/home.html`
- Modify: `.gitignore`

- [ ] **Step 1: Add Swiper CDN to head fragment**

Read `src/main/resources/templates/fragments/head.html`, then add before `</head>`:

```html
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/swiper@12/swiper-bundle.min.css" />
<script src="https://cdn.jsdelivr.net/npm/swiper@12/swiper-bundle.min.js" defer></script>
```

- [ ] **Step 2: Update HomeController to include banners**

Modify `HomeController.java` to inject `BannerService` and add banners to the model:

```java
// Add import:
import com.ptithcm.smartshop.banner.service.BannerService;

// Add field:
private final BannerService bannerService;

// Update constructor:
public HomeController(AuthService authService, BannerService bannerService) {
    this.authService = authService;
    this.bannerService = bannerService;
}

// In the home() method, before return "home":
model.addAttribute("banners", bannerService.findAllActive());
```

The full updated `HomeController.java`:

```java
package com.ptithcm.smartshop.auth.controller;

import com.ptithcm.smartshop.auth.dto.AuthResponse;
import com.ptithcm.smartshop.banner.service.BannerService;
import com.ptithcm.smartshop.security.principal.SessionUser;
import com.ptithcm.smartshop.shared.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final AuthService authService;
    private final BannerService bannerService;

    public HomeController(AuthService authService, BannerService bannerService) {
        this.authService = authService;
        this.bannerService = bannerService;
    }

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        try {
            AuthResponse authResponse = authService.me(request);
            model.addAttribute("auth", authResponse);
            model.addAttribute("sessionUser", authResponse.sessionUser());
            model.addAttribute("user", authResponse.user());
        } catch (UnauthorizedException e) {
            // Guest chưa đăng nhập → vẫn hiển thị trang Home bình thường.
        }
        model.addAttribute("banners", bannerService.findAllActive());
        return "home";
    }
}
```

- [ ] **Step 3: Add Swiper HTML to home.html**

Replace the `<main class="dashboard-shell">` section in `home.html` with:

```html
<main class="dashboard-shell">
    <!-- Banner Carousel -->
    <div th:if="${banners != null and !#lists.isEmpty(banners)}" class="relative w-full">
        <div class="swiper banner-swiper">
            <div class="swiper-wrapper">
                <div th:each="banner : ${banners}" class="swiper-slide relative"
                     th:if="${banner.isActive}">
                    <a th:if="${banner.linkUrl != null and !#strings.isEmpty(banner.linkUrl)}"
                       th:href="${banner.linkUrl}">
                        <img th:src="${banner.imageUrl}"
                             th:alt="${banner.title}"
                             class="w-full h-[300px] md:h-[400px] object-cover"/>
                    </a>
                    <img th:unless="${banner.linkUrl != null and !#strings.isEmpty(banner.linkUrl)}"
                         th:src="${banner.imageUrl}"
                         th:alt="${banner.title}"
                         class="w-full h-[300px] md:h-[400px] object-cover"/>
                </div>
            </div>
            <div class="swiper-pagination"></div>
            <div class="swiper-button-prev"></div>
            <div class="swiper-button-next"></div>
        </div>

        <!-- Admin Inline Controls -->
        <div th:if="${sessionUser != null and #lists.contains(sessionUser.roles, 'ADMIN')}"
             class="absolute top-2 right-2 z-10 flex gap-2">
            <a href="/admin/banners"
               class="bg-white/90 text-gray-800 px-3 py-1 rounded-full text-xs font-medium shadow hover:bg-white">
                Quản lý Banner
            </a>
        </div>
    </div>

    <!-- Existing content -->
    <div class="alert success" th:if="${successMessage}" th:text="${successMessage}"></div>
    <div class="alert error" th:if="${param.error}">Bạn không có quyền truy cập chức năng này.</div>

    <section class="dashboard-grid" th:if="${sessionUser != null}">
        <article class="panel">
            <h2>Thông tin session</h2>
            <ul class="detail-list">
                <li><strong>Session ID:</strong> <span th:text="${auth.sessionId}"></span></li>
                <li><strong>Email:</strong> <span th:text="${sessionUser.email}"></span></li>
                <li><strong>Phone:</strong> <span th:text="${sessionUser.phone}"></span></li>
                <li><strong>Roles:</strong> <span th:text="${#strings.listJoin(sessionUser.roles, ', ')}"></span></li>
            </ul>
        </article>

        <article class="panel">
            <h2>Thông tin người dùng</h2>
            <ul class="detail-list">
                <li><strong>User ID:</strong> <span th:text="${user.id}"></span></li>
                <li><strong>Status:</strong> <span th:text="${user.status}"></span></li>
                <li><strong>Permissions:</strong> <span th:text="${#strings.listJoin(user.permissions, ', ')}"></span></li>
                <li><strong>Updated:</strong> <span th:text="${user.updatedAt}"></span></li>
            </ul>
        </article>
    </section>
</main>

<script th:inline="javascript">
document.addEventListener('DOMContentLoaded', function() {
    if (document.querySelector('.banner-swiper')) {
        new Swiper('.banner-swiper', {
            loop: true,
            effect: 'slide',
            speed: 600,
            autoplay: {
                delay: 4000,
                disableOnInteraction: false,
            },
            pagination: {
                el: '.swiper-pagination',
                clickable: true,
            },
            navigation: {
                nextEl: '.swiper-button-next',
                prevEl: '.swiper-button-prev',
            },
        });
    }
});
</script>
```

- [ ] **Step 4: Update .gitignore**

Read current `.gitignore`, then add:

```
# Banner uploads
src/main/resources/static/uploads/
```

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/templates/fragments/head.html \
          src/main/resources/templates/home.html \
          src/main/java/com/ptithcm/smartshop/auth/controller/HomeController.java \
          .gitignore
git commit -m "feat: integrate Swiper banner carousel on homepage"
```

---

### Task 7: Testing and Polish

**Files:**
- Modify: `.gitignore` (if not done in Task 6)

- [ ] **Step 1: Run full application and verify**

```bash
docker compose up -d
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

- [ ] **Step 2: Verify homepage loads with empty banner state**

Navigate to `http://localhost:8080/` — should load without errors even with zero banners.

- [ ] **Step 3: Seed test banner data**

In PostgreSQL, insert a test banner:

```sql
INSERT INTO banners (title, image_url, link_url, display_order, is_active)
VALUES ('Test Banner', 'https://via.placeholder.com/1200x400', '/', 0, true);
```

Refresh homepage — should see the banner carousel.

- [ ] **Step 4: Verify admin access**

Login as admin (admin@smartshop.local / admin123 or whatever seed credentials exist) and verify:
- "Quản lý Banner" button appears on homepage
- `/admin/banners` page is accessible
- Create, update, delete work

- [ ] **Step 5: Run existing tests**

```bash
./mvnw test
```

Expected: All existing tests pass.

- [ ] **Step 6: Final commit**

```bash
git add .
git commit -m "chore: verify banner module integration and fix any issues"
```
