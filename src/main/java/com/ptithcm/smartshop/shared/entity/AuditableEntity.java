package com.ptithcm.smartshop.shared.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
/**
 * Base entity dùng chung cho các bảng cần theo dõi lịch sử tạo/cập nhật.
 *
 * Cấp class này chuẩn hóa 4 thông tin audit:
 * - createdAt: thời điểm tạo bản ghi
 * - updatedAt: thời điểm cập nhật gần nhất
 * - createdBy: ai tạo bản ghi
 * - updatedBy: ai cập nhật gần nhất
 */
public abstract class AuditableEntity extends BaseUuidEntity {

	// Thời điểm bản ghi được tạo lần đầu (do Hibernate tự gán).
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	// Thời điểm bản ghi được cập nhật gần nhất (do Hibernate tự gán).
	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	// Người tạo bản ghi (được điền tự động từ AuditorAware).
	@CreatedBy
	@Column(name = "created_by", length = 100)
	private String createdBy;

	// Người cập nhật bản ghi gần nhất (được điền tự động từ AuditorAware).
	@LastModifiedBy
	@Column(name = "updated_by", length = 100)
	private String updatedBy;

	/**
	 * Lấy thời điểm tạo bản ghi.
	 * BƯỚC 1: Hibernate gán giá trị khi insert.
	 * BƯỚC 2: Getter trả về để tầng service/api đọc thông tin audit.
	 */
	public Instant getCreatedAt() {
		return createdAt;
	}

	/**
	 * Lấy thời điểm cập nhật gần nhất của bản ghi.
	 * BƯỚC 1: Hibernate cập nhật giá trị mỗi lần update.
	 * BƯỚC 2: Getter trả về cho các luồng hiển thị/đối soát.
	 */
	public Instant getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * Lấy định danh người tạo bản ghi.
	 * BƯỚC 1: Spring Data JPA lấy auditor từ AuditorAware.
	 * BƯỚC 2: Gán vào field createdBy khi insert.
	 * BƯỚC 3: Getter trả về giá trị đã lưu.
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * Lấy định danh người cập nhật gần nhất.
	 * BƯỚC 1: Spring Data JPA lấy auditor từ AuditorAware.
	 * BƯỚC 2: Gán vào field updatedBy khi update.
	 * BƯỚC 3: Getter trả về giá trị đã lưu.
	 */
	public String getUpdatedBy() {
		return updatedBy;
	}
}
