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
package org.springframework.samples.petclinic.vet;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Your Name
 */
@Controller
class AppointmentController {

    private static final String VIEWS_APPOINTMENT_CREATE_OR_UPDATE_FORM = "vets/createOrUpdateAppointmentForm";
    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    private final AppointmentService appointmentService;
    private final VetRepository vets;
    private final OwnerRepository owners;

    public AppointmentController(AppointmentService appointmentService, VetRepository vets, OwnerRepository owners) {
        this.appointmentService = appointmentService;
        this.vets = vets;
        this.owners = owners;
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @ModelAttribute("statuses")
    public List<Appointment.Status> populateStatuses() {
        return List.of(Appointment.Status.values());
    }

    @ModelAttribute("timeSlots")
    public List<Appointment.TimeSlot> populateTimeSlots() {
        return List.of(Appointment.TimeSlot.values());
    }

    @ModelAttribute("vets")
    public List<Vet> populateVets() {
        return this.vets.findAll();
    }

    @ModelAttribute("appointment")
    public Appointment loadAppointmentWithPetAndVet(
            @PathVariable(name = "appointmentId", required = false) Integer appointmentId,
            @PathVariable(name = "petId", required = false) Integer petId,
            @PathVariable(name = "vetId", required = false) Integer vetId,
            Map<String, Object> model) {

        Appointment appointment = new Appointment();

        if (petId != null) {
            Optional<Owner> optionalOwner = owners.findById(petId / 1000); // 假设petId的前三位是ownerId
            if (optionalOwner.isPresent()) {
                Owner owner = optionalOwner.get();
                Pet pet = owner.getPet(petId);
                if (pet != null) {
                    appointment.setPet(pet);
                    model.put("pet", pet);
                    model.put("owner", owner);
                }
            }
        }

        if (vetId != null) {
            Optional<Vet> optionalVet = vets.findById(vetId);
            if (optionalVet.isPresent()) {
                Vet vet = optionalVet.get();
                appointment.setVet(vet);
                model.put("vet", vet);
            }
        }

        if (appointmentId != null) {
            Optional<Appointment> optionalAppointment = appointmentService.findById(appointmentId);
            if (optionalAppointment.isPresent()) {
                appointment = optionalAppointment.get();
            }
        }

        return appointment;
    }

    @GetMapping("/vets/{vetId}/appointments/new")
    public String initNewAppointmentForm(@PathVariable("vetId") int vetId, Model model) {
        Optional<Vet> optionalVet = vets.findById(vetId);
        if (optionalVet.isPresent()) {
            model.addAttribute("vet", optionalVet.get());
        }
        return VIEWS_APPOINTMENT_CREATE_OR_UPDATE_FORM;
    }

    @GetMapping("/owners/{ownerId}/pets/{petId}/appointments/new")
    public String initNewAppointmentFormForPet(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId, Model model) {
        Optional<Owner> optionalOwner = owners.findById(ownerId);
        if (optionalOwner.isPresent()) {
            Owner owner = optionalOwner.get();
            Pet pet = owner.getPet(petId);
            if (pet != null) {
                model.addAttribute("owner", owner);
                model.addAttribute("pet", pet);
            }
        }
        return VIEWS_APPOINTMENT_CREATE_OR_UPDATE_FORM;
    }

    @GetMapping("/appointments/{appointmentId}/edit")
    public String initUpdateAppointmentForm() {
        return VIEWS_APPOINTMENT_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping("/appointments/{appointmentId}/edit")
    public String processUpdateAppointmentForm(@Valid Appointment appointment, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return VIEWS_APPOINTMENT_CREATE_OR_UPDATE_FORM;
        }

        try {
            appointmentService.updateAppointment(appointment);
            redirectAttributes.addFlashAttribute("message", "Appointment updated successfully");
            return "redirect:/vets/" + appointment.getVet().getId() + "/schedule";
        } catch (AppointmentConflictException ex) {
            result.rejectValue("appointmentDate", "conflict", ex.getMessage());
            return VIEWS_APPOINTMENT_CREATE_OR_UPDATE_FORM;
        }
    }

    @PostMapping("/vets/{vetId}/appointments/new")
    public String processNewAppointmentForm(@Valid Appointment appointment, BindingResult result, @PathVariable("vetId") int vetId, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return VIEWS_APPOINTMENT_CREATE_OR_UPDATE_FORM;
        }

        Optional<Vet> optionalVet = vets.findById(vetId);
        if (optionalVet.isPresent()) {
            appointment.setVet(optionalVet.get());
            try {
                appointmentService.createAppointment(appointment);
                redirectAttributes.addFlashAttribute("message", "Appointment created successfully");
                return "redirect:/vets/" + vetId + "/schedule";
            } catch (AppointmentConflictException ex) {
                result.rejectValue("appointmentDate", "conflict", ex.getMessage());
                return VIEWS_APPOINTMENT_CREATE_OR_UPDATE_FORM;
            }
        }

        redirectAttributes.addFlashAttribute("error", "Vet not found");
        return "redirect:/vets";
    }

    @PostMapping("/owners/{ownerId}/pets/{petId}/appointments/new")
    public String processNewAppointmentFormForPet(@Valid Appointment appointment, BindingResult result, 
            @PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId, 
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return VIEWS_APPOINTMENT_CREATE_OR_UPDATE_FORM;
        }

        Optional<Owner> optionalOwner = owners.findById(ownerId);
        if (optionalOwner.isPresent()) {
            Owner owner = optionalOwner.get();
            Pet pet = owner.getPet(petId);
            if (pet != null) {
                appointment.setPet(pet);
                try {
                    appointmentService.createAppointment(appointment);
                    redirectAttributes.addFlashAttribute("message", "Appointment created successfully");
                    return "redirect:/owners/" + ownerId;
                } catch (AppointmentConflictException ex) {
                    result.rejectValue("appointmentDate", "conflict", ex.getMessage());
                    return VIEWS_APPOINTMENT_CREATE_OR_UPDATE_FORM;
                }
            }
        }

        redirectAttributes.addFlashAttribute("error", "Owner or pet not found");
        return "redirect:/owners";
    }

    @PostMapping("/appointments/{appointmentId}/delete")
    public String processDeleteAppointmentForm(@PathVariable("appointmentId") int appointmentId, RedirectAttributes redirectAttributes) {
        Optional<Appointment> optionalAppointment = appointmentService.findById(appointmentId);
        if (optionalAppointment.isPresent()) {
            Appointment appointment = optionalAppointment.get();
            int vetId = appointment.getVet().getId();
            appointmentService.deleteAppointment(appointmentId);
            redirectAttributes.addFlashAttribute("message", "Appointment deleted successfully");
            return "redirect:/vets/" + vetId + "/schedule";
        }
        redirectAttributes.addFlashAttribute("error", "Appointment not found");
        return "redirect:/vets";
    }

    // 预约提醒功能（模拟）
    @GetMapping("/appointments/reminders")
    public String sendAppointmentReminders() {
        List<Appointment> upcomingAppointments = appointmentService.findUpcomingAppointments();
        for (Appointment appointment : upcomingAppointments) {
            logger.info("Reminder: Appointment for {} with {} on {} at {}",
                    appointment.getPet().getName(),
                    appointment.getVet().getFullName(),
                    appointment.getAppointmentDate(),
                    appointment.getTimeSlot());
        }
        return "redirect:/vets";
    }
}