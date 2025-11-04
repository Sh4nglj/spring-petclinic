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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.samples.petclinic.model.City;
import org.springframework.samples.petclinic.owner.CityRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Component that loads city data from a JSON file on application startup.
 *
 * @author Trae AI
 */
@Component
public class CityDataLoader implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(CityDataLoader.class);

	private static final String CITY_DATA_FILE = "classpath:data/cities.json";

	private final CityRepository cityRepository;

	private final ObjectMapper objectMapper;

	public CityDataLoader(CityRepository cityRepository, ObjectMapper objectMapper) {
		this.cityRepository = cityRepository;
		this.objectMapper = objectMapper;
	}

	@Override
	public void run(String... args) throws Exception {
		// Only load data if cities table is empty
		if (cityRepository.count() == 0) {
			try {
				File file = ResourceUtils.getFile(CITY_DATA_FILE);
				List<City> cities = objectMapper.readValue(file, new TypeReference<List<City>>() {
				});
				cityRepository.saveAll(cities);
				logger.info("Successfully loaded {} cities from {}", cities.size(), CITY_DATA_FILE);
			}
			catch (IOException e) {
				logger.error("Failed to load city data from {}", CITY_DATA_FILE, e);
			}
		}
		else {
			logger.info("City data already exists, skipping load");
		}
	}

}