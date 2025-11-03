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
package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

/**
 * Simple JavaBean domain object representing a visit.
 *
 * @author Ken Krebs
 * @author Dave Syer
 */
@Entity
@Table(name = "visits")
public class Visit extends BaseEntity {

	@Column(name = "visit_date")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private @Nullable LocalDate date;

	@NotBlank
	private @Nullable String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "vaccination_status")
	private @Nullable VaccinationStatus vaccinationStatus;

	@Column(name = "vaccination_date")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private @Nullable LocalDate vaccinationDate;

	/**
	 * Creates a new instance of Visit for the current date
	 */
	public Visit() {
		this.date = LocalDate.now();
	}

	public @Nullable LocalDate getDate() {
		return this.date;
	}

	public void setDate(@Nullable LocalDate date) {
		this.date = date;
	}

	public @Nullable String getDescription() {
		return this.description;
	}

	public void setDescription(@Nullable String description) {
		this.description = description;
	}

	public @Nullable VaccinationStatus getVaccinationStatus() {
		return vaccinationStatus;
	}

	public void setVaccinationStatus(@Nullable VaccinationStatus vaccinationStatus) {
		this.vaccinationStatus = vaccinationStatus;
	}

	public @Nullable LocalDate getVaccinationDate() {
		return vaccinationDate;
	}

	public void setVaccinationDate(@Nullable LocalDate vaccinationDate) {
		this.vaccinationDate = vaccinationDate;
	}

	/**
	 * Enum representing the vaccination status of a pet.
	 */
	enum VaccinationStatus {

		FULLY_VACCINATED("已接种"), NOT_VACCINATED("未接种"), PARTIALLY_VACCINATED("部分接种"), VACCINATION_EXPIRED("免疫期已过");

		private final String displayName;

		VaccinationStatus(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}

	}

}
