package com.ptithcm.smartshop.address.entity;

import com.ptithcm.smartshop.shared.entity.AuditableEntity;
import com.ptithcm.smartshop.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "addresses")
public class Address extends AuditableEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "receiver_name", nullable = false, length = 120)
	private String receiverName;

	@Column(nullable = false, length = 20)
	private String phone;

	@Column(nullable = false, length = 100)
	private String province;

	@Column(nullable = false, length = 100)
	private String district;

	@Column(nullable = false, length = 100)
	private String ward;

	@Column(nullable = false, length = 255)
	private String detail;

	@Column(name = "is_default", nullable = false)
	private boolean isDefault;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getReceiverName() {
		return receiverName;
	}

	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getWard() {
		return ward;
	}

	public void setWard(String ward) {
		this.ward = ward;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean aDefault) {
		isDefault = aDefault;
	}
}

