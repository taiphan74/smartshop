# Banner Module Design Spec

## Overview

Add a banner carousel to the homepage using Swiper 12 (CDN). Banners are managed via inline admin controls — when an ADMIN user is logged in, each banner shows edit/delete buttons and there's an "Add Banner" button at the end of the carousel.

## Data Model

**Table `banners`**:
- `id` UUID (PK, generated)
- `title` VARCHAR(255) — banner title
- `image_url` VARCHAR(500) NOT NULL — path to uploaded image
- `link_url` VARCHAR(500) — destination URL when clicked
- `display_order` INT NOT NULL DEFAULT 0 — sort order
- `is_active` BOOLEAN NOT NULL DEFAULT true — show/hide
- `created_at`, `updated_at`, `created_by`, `updated_by` (audit columns)

## Architecture

### Backend
- New `banner/` package following existing patterns (entity extends `AuditableEntity`, repository extends `JpaRepository<UUID>`, service interface + impl)
- `AdminBannerController` (`@Controller`, Thymeleaf views) under `/admin/banners`
- Uses `@RequirePermission` with new `BANNER_*` permissions
- File upload: `MultipartFile` → saved to `static/uploads/banners/`, filename = UUID + extension

### Frontend
- Homepage (`home.html`): Swiper carousel rendered from `banners` model attribute
- Inline admin controls: edit/delete buttons overlaid on each banner, "Add" button at end — only visible to ADMIN role
- Edit uses modal overlay with form
- Add uses modal overlay with form + file upload
- Swiper CDN: swiper@12 (CSS + JS)

### Security
- New permissions: `BANNER_READ`, `BANNER_CREATE`, `BANNER_UPDATE`, `BANNER_DELETE`
- Seed into RBAC data, assign to ADMIN role
- `SecurityConfig`: allow `/uploads/**` for static image serving
- Upload path `/admin/banners/upload` requires `BANNER_CREATE` or `BANNER_UPDATE`

## File Structure

```
src/main/java/com/ptithcm/smartshop/banner/
  entity/Banner.java                    # extends AuditableEntity
  repository/BannerRepository.java      # JpaRepository
  service/BannerService.java            # interface
  service/impl/BannerServiceImpl.java   # impl
  dto/BannerForm.java                   # form DTO for Thymeleaf
  controller/AdminBannerController.java # @Controller
  config/BannerUploadConfig.java        # upload path config

src/main/resources/
  db/changelog/changes/009-create-banner-table.yaml
  db/changelog/changes/010-seed-banner-permissions.yaml
  static/uploads/banners/               # upload directory (gitignored)
  templates/admin/banners/form.html     # edit/create modal (Thymeleaf fragment)

src/main/resources/templates/home.html  # modified — add Swiper
src/main/java/com/ptithcm/smartshop/security/rbac/Permission.java  # modified — add BANNER_*
src/main/java/com/ptithcm/smartshop/security/config/SecurityConfig.java  # modified — allow /uploads/**
src/main/java/com/ptithcm/smartshop/auth/controller/HomeController.java  # modified — add banners to model
.gitignore                              # modified — ignore uploads