/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple JavaBean domain object representing a city with hierarchical structure.
 *
 * @author Trae AI
 */
@Entity
@Table(name = "cities",
		indexes = { @Index(name = "idx_city_code", columnList = "code"),
				@Index(name = "idx_city_parent_code", columnList = "parent_code"),
				@Index(name = "idx_city_level", columnList = "level") })
public class City extends NamedEntity {

	@Column(name = "code", unique = true, nullable = false, length = 10)
	@NotBlank
	private String code;

	@Column(name = "level", nullable = false)
	@NotNull
	private Integer level;

	@Column(name = "parent_code", length = 10)
	private @Nullable String parentCode;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "parent_code", referencedColumnName = "code", insertable = false, updatable = false)
	private @Nullable City parent;

	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
	private Set<City> children = new HashSet<>();

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public @Nullable String getParentCode() {
		return parentCode;
	}

	public void setParentCode(@Nullable String parentCode) {
		this.parentCode = parentCode;
	}

	public @Nullable City getParent() {
		return parent;
	}

	public void setParent(@Nullable City parent) {
		this.parent = parent;
	}

	public Set<City> getChildren() {
		return children;
	}

	public void setChildren(Set<City> children) {
		this.children = children;
	}

	@Override
	public String toString() {
		return "City{" + "id=" + getId() + ", name='" + getName() + '\'' + ", code='" + code + '\'' + ", level=" + level
				+ ", parentCode='" + parentCode + '\'' + '}';
	}

}