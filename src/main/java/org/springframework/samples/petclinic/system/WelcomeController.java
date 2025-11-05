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
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class WelcomeController {

	private final StatsService statsService;

	public WelcomeController(StatsService statsService) {
		this.statsService = statsService;
	}

	@GetMapping("/")
	public String welcome(Model model) {
		// Add statistics to model
		model.addAttribute("stats", statsService.getAllStats());

		// Add welcome message based on time
		String welcomeMessage = getWelcomeMessage();
		model.addAttribute("welcomeMessage", welcomeMessage);

		return "welcome";
	}

	/**
	 * Get welcome message based on current time.
	 * @return welcome message
	 */
	private String getWelcomeMessage() {
		int hour = java.time.LocalTime.now().getHour();
		if (hour < 12) {
			return "上午好";
		}
		else if (hour < 18) {
			return "下午好";
		}
		else {
			return "晚上好";
		}
	}

}
