package com.ptithcm.smartshop.shared.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@MappedSuperclass
public abstract class BaseUuidEntity {

	@Id
	@GeneratedValue
	@UuidGenerator
	@Column(nullable = false, updatable = false, length = 36)
	private UUID id;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		BaseUuidEntity that = (BaseUuidEntity) o;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}

