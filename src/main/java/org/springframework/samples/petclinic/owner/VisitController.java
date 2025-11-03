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

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 * @author Wick Dynex
 */
@Controller
class VisitController {

	private final OwnerRepository owners;

	public VisitController(OwnerRepository owners) {
		this.owners = owners;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	/**
	 * Called before each and every @RequestMapping annotated method. 2 goals: - Make sure
	 * we always have fresh data - Since we do not use the session scope, make sure that
	 * Pet object always has an id (Even though id is not part of the form fields)
	 * @param petId
	 * @return Pet
	 */
	@ModelAttribute("visit")
	public Visit loadPetWithVisit(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
			Map<String, Object> model) {
		Optional<Owner> optionalOwner = owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));

		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException(
					"Pet with id " + petId + " not found for owner with id " + ownerId + ".");
		}
		model.put("pet", pet);
		model.put("owner", owner);

		Visit visit = new Visit();
		pet.addVisit(visit);
		return visit;
	}

	// Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is
	// called
	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String initNewVisitForm() {
		return "pets/createOrUpdateVisitForm";
	}

	// Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is
	// called
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String processNewVisitForm(@ModelAttribute Owner owner, @PathVariable int petId, @Valid Visit visit,
			BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "pets/createOrUpdateVisitForm";
		}

		// Validate vaccination date is not before pet's birth date
		Pet pet = owner.getPet(petId);
		if (pet != null && visit.getVaccinationDate() != null && pet.getBirthDate() != null) {
			if (visit.getVaccinationDate().isBefore(pet.getBirthDate())) {
				result.rejectValue("vaccinationDate", "error.vaccinationDate.beforeBirthDate",
						"Vaccination date cannot be before pet's birth date");
				return "pets/createOrUpdateVisitForm";
			}
		}

		owner.addVisit(petId, visit);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "Your visit has been booked");
		return "redirect:/owners/{ownerId}";
	}

	// 编辑疫苗接种记录的GET方法
	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/{visitId}/edit")
	public String initEditVisitForm(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
			@PathVariable("visitId") int visitId, Map<String, Object> model) {
		Optional<Owner> optionalOwner = owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));

		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException(
					"Pet with id " + petId + " not found for owner with id " + ownerId + ".");
		}

		Visit visit = pet.getVisit(visitId);
		if (visit == null) {
			throw new IllegalArgumentException(
					"Visit with id " + visitId + " not found for pet with id " + petId + ".");
		}

		model.put("pet", pet);
		model.put("owner", owner);
		model.put("visit", visit);

		return "pets/createOrUpdateVisitForm";
	}

	// 编辑疫苗接种记录的POST方法
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/{visitId}/edit")
	public String processEditVisitForm(@ModelAttribute Owner owner, @PathVariable int petId, @PathVariable int visitId,
			@Valid Visit visit, BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "pets/createOrUpdateVisitForm";
		}

		// Validate vaccination date is not before pet's birth date
		Pet pet = owner.getPet(petId);
		if (pet != null && visit.getVaccinationDate() != null && pet.getBirthDate() != null) {
			if (visit.getVaccinationDate().isBefore(pet.getBirthDate())) {
				result.rejectValue("vaccinationDate", "error.vaccinationDate.beforeBirthDate",
						"Vaccination date cannot be before pet's birth date");
				return "pets/createOrUpdateVisitForm";
			}
		}

		// 更新访问记录
		owner.updateVisit(petId, visitId, visit);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "Your visit has been updated");
		return "redirect:/owners/{ownerId}";
	}

	// 删除疫苗接种记录的POST方法
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/{visitId}/delete")
	public String deleteVisit(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
			@PathVariable("visitId") int visitId, RedirectAttributes redirectAttributes) {
		Optional<Owner> optionalOwner = owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));

		// 删除访问记录
		owner.removeVisit(petId, visitId);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "Your visit has been deleted");
		return "redirect:/owners/{ownerId}";
	}

}
