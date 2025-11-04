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
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Service class for {@link Appointment} domain objects.
 *
 * @author Your Name
 */
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final VetRepository vetRepository;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository, VetRepository vetRepository) {
        this.appointmentRepository = appointmentRepository;
        this.vetRepository = vetRepository;
    }

    /**
     * Retrieve all appointments.
     * @return a list of appointments
     */
    @Transactional(readOnly = true)
    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    /**
     * Retrieve an appointment by id.
     * @param id the id of the appointment
     * @return an optional containing the appointment if found
     */
    @Transactional(readOnly = true)
    public Optional<Appointment> findById(Integer id) {
        return appointmentRepository.findById(id);
    }

    /**
     * Retrieve appointments by vet id.
     * @param vetId the id of the vet
     * @return a list of appointments
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByVetId(Integer vetId) {
        return appointmentRepository.findByVetId(vetId);
    }

    /**
     * Retrieve appointments by vet id and week.
     * @param vetId the id of the vet
     * @param weekStart the start date of the week
     * @param weekEnd the end date of the week
     * @return a list of appointments
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByVetAndWeek(Integer vetId, LocalDate weekStart, LocalDate weekEnd) {
        return appointmentRepository.findByVetIdAndWeek(vetId, weekStart, weekEnd);
    }

    /**
     * Retrieve appointments by pet id.
     * @param petId the id of the pet
     * @return a list of appointments
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByPetId(Integer petId) {
        return appointmentRepository.findByPetId(petId);
    }

    /**
     * Check if an appointment exists for a vet at a specific time slot.
     * @param vetId the id of the vet
     * @param appointmentDate the date of the appointment
     * @param timeSlot the time slot of the appointment
     * @return true if an appointment exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isAppointmentSlotAvailable(Integer vetId, LocalDate appointmentDate, Appointment.TimeSlot timeSlot) {
        return !appointmentRepository.existsByVetIdAndAppointmentDateAndTimeSlot(vetId, appointmentDate, timeSlot);
    }

    /**
     * Create a new appointment.
     * @param appointment the appointment to create
     * @return the created appointment
     * @throws IllegalArgumentException if the appointment slot is not available
     */
    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        Assert.notNull(appointment, "Appointment must not be null");
        Assert.notNull(appointment.getVet(), "Vet must not be null");
        Assert.notNull(appointment.getPet(), "Pet must not be null");
        Assert.notNull(appointment.getAppointmentDate(), "Appointment date must not be null");
        Assert.notNull(appointment.getTimeSlot(), "Time slot must not be null");
        
        // Check if the appointment slot is available
        if (!isAppointmentSlotAvailable(appointment.getVet().getId(), appointment.getAppointmentDate(), appointment.getTimeSlot())) {
            throw new AppointmentConflictException("Appointment slot is already booked for this vet at the selected time");
        }
        
        // Set default status and timestamps
        if (appointment.getStatus() == null) {
            appointment.setStatus(Appointment.Status.PENDING_CONFIRMATION);
        }
        
        LocalDateTime now = LocalDateTime.now();
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);
        
        // Save the appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // Add the appointment to the vet's list
        Vet vet = appointment.getVet();
        vet.addAppointment(savedAppointment);
        vetRepository.save(vet);
        
        return savedAppointment;
    }

    /**
     * Update an existing appointment.
     * @param appointment the appointment to update
     * @return the updated appointment
     * @throws IllegalArgumentException if the appointment slot is not available for the new time
     */
    @Transactional
    public Appointment updateAppointment(Appointment appointment) {
        Assert.notNull(appointment, "Appointment must not be null");
        Assert.notNull(appointment.getId(), "Appointment id must not be null");
        Assert.notNull(appointment.getVet(), "Vet must not be null");
        Assert.notNull(appointment.getPet(), "Pet must not be null");
        Assert.notNull(appointment.getAppointmentDate(), "Appointment date must not be null");
        Assert.notNull(appointment.getTimeSlot(), "Time slot must not be null");
        
        // Check if the appointment exists
        Optional<Appointment> existingAppointmentOptional = appointmentRepository.findById(appointment.getId());
        if (existingAppointmentOptional.isEmpty()) {
            throw new IllegalArgumentException("Appointment not found");
        }
        
        Appointment existingAppointment = existingAppointmentOptional.get();
        
        // Check if the time slot has changed
        if (!existingAppointment.getAppointmentDate().equals(appointment.getAppointmentDate()) ||
            !existingAppointment.getTimeSlot().equals(appointment.getTimeSlot()) ||
            !existingAppointment.getVet().getId().equals(appointment.getVet().getId())) {
            
            // Check if the new appointment slot is available
            if (!isAppointmentSlotAvailable(appointment.getVet().getId(), appointment.getAppointmentDate(), appointment.getTimeSlot())) {
                throw new AppointmentConflictException("Appointment slot is already booked for this vet at the selected time");
            }
        }
        
        // Update the appointment
        existingAppointment.setAppointmentDate(appointment.getAppointmentDate());
        existingAppointment.setTimeSlot(appointment.getTimeSlot());
        existingAppointment.setPet(appointment.getPet());
        existingAppointment.setVet(appointment.getVet());
        existingAppointment.setStatus(appointment.getStatus());
        existingAppointment.setNotes(appointment.getNotes());
        existingAppointment.setUpdatedAt(LocalDateTime.now());
        
        return appointmentRepository.save(existingAppointment);
    }

    /**
     * Delete an appointment.
     * @param id the id of the appointment to delete
     */
    @Transactional
    public void deleteAppointment(Integer id) {
        appointmentRepository.deleteById(id);
    }

    /**
     * Get the week start and end dates for a given date.
     * @param date the date
     * @return an array containing the week start and end dates
     */
    public LocalDate[] getWeekDates(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        LocalDate weekStart = date.with(weekFields.dayOfWeek(), 1);
        LocalDate weekEnd = date.with(weekFields.dayOfWeek(), 7);
        return new LocalDate[] { weekStart, weekEnd };
    }
    
    /**
     * Find upcoming appointments that are scheduled within the next 24 hours.
     * @return a list of upcoming appointments
     */
    @Transactional(readOnly = true)
    public List<Appointment> findUpcomingAppointments() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate currentDate = now.toLocalDate();
        int currentHour = now.getHour();
        Appointment.TimeSlot currentTimeSlot = determineCurrentTimeSlot(currentHour);
        LocalDate currentDatePlusOne = currentDate.plusDays(1);
        
        return appointmentRepository.findUpcomingAppointments(currentDate, currentTimeSlot, currentDatePlusOne);
    }
    
    /**
     * Determine the current time slot based on the current hour.
     * @param currentHour the current hour
     * @return the current time slot
     */
    private Appointment.TimeSlot determineCurrentTimeSlot(int currentHour) {
        if (currentHour < 10) {
            return Appointment.TimeSlot.SLOT_09_10;
        } else if (currentHour < 11) {
            return Appointment.TimeSlot.SLOT_10_11;
        } else if (currentHour < 12) {
            return Appointment.TimeSlot.SLOT_11_12;
        } else if (currentHour < 15) {
            return Appointment.TimeSlot.SLOT_14_15;
        } else if (currentHour < 16) {
            return Appointment.TimeSlot.SLOT_15_16;
        } else {
            return Appointment.TimeSlot.SLOT_16_17;
        }
    }
}