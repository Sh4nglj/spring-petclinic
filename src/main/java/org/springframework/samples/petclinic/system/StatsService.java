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

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for statistics operations.
 *
 * @author Your Name
 */
@Service
public class StatsService {

	private final OwnerRepository ownerRepository;

	private final VetRepository vetRepository;

	public StatsService(OwnerRepository ownerRepository, VetRepository vetRepository) {
		this.ownerRepository = ownerRepository;
		this.vetRepository = vetRepository;
	}

	/**
	 * Get all statistics data.
	 * @return a map containing all statistics
	 */
	@Cacheable(value = "stats", key = "'allStats'", cacheManager = "cacheManager")
	@Transactional(readOnly = true)
	public Map<String, Object> getAllStats() {
		Map<String, Object> stats = new HashMap<>();

		// Basic counts
		stats.put("totalOwners", ownerRepository.count());
		stats.put("totalPets", ownerRepository.countPets());
		stats.put("totalVets", vetRepository.count());
		stats.put("todayVisits", ownerRepository.countVisitsToday());
		stats.put("thisMonthVisits", ownerRepository.countVisitsThisMonth());

		// Last 7 days visit trend
		LocalDate startDate = LocalDate.now().minusDays(6); // 7 days including today
		List<Object[]> visitCounts = ownerRepository.getVisitCountsLast7Days(startDate);
		stats.put("visitTrend", formatVisitTrend(visitCounts, startDate));

		// Pet type distribution
		List<Object[]> petTypeCounts = ownerRepository.getPetTypeDistribution();
		stats.put("petTypeDistribution", formatPetTypeDistribution(petTypeCounts));

		// Recent visits (last 10)
		stats.put("recentVisits", ownerRepository.findRecentVisits(PageRequest.of(0, 10)));

		return stats;
	}

	/**
	 * Format visit trend data for chart display.
	 * @param visitCounts raw data from database
	 * @param startDate the start date of the trend
	 * @return formatted data for chart
	 */
	private List<Map<String, Object>> formatVisitTrend(List<Object[]> visitCounts, LocalDate startDate) {
		List<Map<String, Object>> formatted = new ArrayList<>();
		Map<LocalDate, Long> countMap = new HashMap<>();

		// Initialize map with all dates in range
		for (int i = 0; i < 7; i++) {
			LocalDate date = startDate.plusDays(i);
			countMap.put(date, 0L);
		}

		// Fill in actual counts
		for (Object[] result : visitCounts) {
			LocalDate date = (LocalDate) result[0];
			Long count = (Long) result[1];
			countMap.put(date, count);
		}

		// Convert to list in date order
		for (int i = 0; i < 7; i++) {
			LocalDate date = startDate.plusDays(i);
			Map<String, Object> entry = new HashMap<>();
			entry.put("date", date.toString());
			entry.put("count", countMap.get(date));
			formatted.add(entry);
		}

		return formatted;
	}

	/**
	 * Format pet type distribution data for chart display.
	 * @param petTypeCounts raw data from database
	 * @return formatted data for chart
	 */
	private List<Map<String, Object>> formatPetTypeDistribution(List<Object[]> petTypeCounts) {
		List<Map<String, Object>> formatted = new ArrayList<>();

		for (Object[] result : petTypeCounts) {
			String typeName = (String) result[0];
			Long count = (Long) result[1];
			Map<String, Object> entry = new HashMap<>();
			entry.put("name", typeName);
			entry.put("value", count);
			formatted.add(entry);
		}

		return formatted;
	}

}