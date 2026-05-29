package com.ptithcm.smartshop.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class AdminCategoryForm {
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;
    @NotBlank(message = "Slug không được để trống")
    private String slug;
    @NotBlank(message = "Path không được để trống")
    private String path;
    @NotNull(message = "Level không được để trống")
    @Min(value = 0, message = "Level không được âm")
    private Integer level;
    private UUID parentId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }
}
