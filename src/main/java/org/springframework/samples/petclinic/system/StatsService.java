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
package org.springframework.samples.petclinic.system;

import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for statistics functionality.
 *
 * @author Trae AI
 */
@Service
public class StatsService {

	private final OwnerRepository ownerRepository;

	public StatsService(OwnerRepository ownerRepository) {
		this.ownerRepository = ownerRepository;
	}

	/**
	 * Get the number of owners per city.
	 * @return a map of city names to owner counts
	 */
	public Map<String, Long> getOwnersByCity() {
		return ownerRepository.findAll()
			.stream()
			.filter(owner -> owner.getCity() != null)
			.collect(Collectors.groupingBy(owner -> {
				String cityName = owner.getCity().getName();
				if (owner.getCity().getParent() != null) {
					cityName = owner.getCity().getParent().getName() + cityName;
					if (owner.getCity().getParent().getParent() != null) {
						cityName = owner.getCity().getParent().getParent().getName() + cityName;
					}
				}
				return cityName;
			}, Collectors.counting()));
	}

}