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
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository class for {@link Appointment} domain objects.
 *
 * @author Your Name
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    /**
     * Retrieve {@link Appointment}s by vet id.
     * @param vetId the id of the vet
     * @return a list of appointments
     */
    List<Appointment> findByVetId(Integer vetId);
    
    /**
     * Retrieve {@link Appointment}s by vet id and appointment date.
     * @param vetId the id of the vet
     * @param appointmentDate the date of the appointment
     * @return a list of appointments
     */
    List<Appointment> findByVetIdAndAppointmentDate(Integer vetId, LocalDate appointmentDate);
    
    /**
     * Retrieve {@link Appointment}s by vet id and week.
     * @param vetId the id of the vet
     * @param weekStart the start date of the week
     * @param weekEnd the end date of the week
     * @return a list of appointments
     */
    @Query("SELECT a FROM Appointment a WHERE a.vet.id = :vetId AND a.appointmentDate BETWEEN :weekStart AND :weekEnd")
    List<Appointment> findByVetIdAndWeek(@Param("vetId") Integer vetId, @Param("weekStart") LocalDate weekStart, @Param("weekEnd") LocalDate weekEnd);
    
    /**
     * Check if an appointment exists for a vet at a specific time slot.
     * @param vetId the id of the vet
     * @param appointmentDate the date of the appointment
     * @param timeSlot the time slot of the appointment
     * @return true if an appointment exists, false otherwise
     */
    boolean existsByVetIdAndAppointmentDateAndTimeSlot(Integer vetId, LocalDate appointmentDate, Appointment.TimeSlot timeSlot);
    
    /**
     * Retrieve {@link Appointment}s by pet id.
     * @param petId the id of the pet
     * @return a list of appointments
     */
    List<Appointment> findByPetId(Integer petId);
    
    /**
     * Retrieve upcoming {@link Appointment}s for a vet.
     * @param vetId the id of the vet
     * @param currentDate the current date
     * @return a list of upcoming appointments
     */
    List<Appointment> findByVetIdAndAppointmentDateGreaterThanEqualOrderByAppointmentDateAscTimeSlotAsc(Integer vetId, LocalDate currentDate);
    
    /**
     * Retrieve upcoming {@link Appointment}s within the next 24 hours.
     * @param currentDate the current date
     * @param currentTime the current time
     * @return a list of upcoming appointments
     */
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate = :currentDate AND a.timeSlot >= :currentTime OR a.appointmentDate = :currentDatePlusOne")
    List<Appointment> findUpcomingAppointments(@Param("currentDate") LocalDate currentDate, @Param("currentTime") Appointment.TimeSlot currentTime, @Param("currentDatePlusOne") LocalDate currentDatePlusOne);
}