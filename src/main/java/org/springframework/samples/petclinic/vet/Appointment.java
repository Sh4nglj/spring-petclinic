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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

/**
 * Simple JavaBean domain object representing an appointment.
 *
 * @author Trae AI
 */
@Entity
@Table(name = "appointments")
public class Appointment extends BaseEntity {

    public enum TimeSlot {
        SLOT_0900_1000("09:00-10:00"),
        SLOT_1000_1100("10:00-11:00"),
        SLOT_1100_1200("11:00-12:00"),
        SLOT_1400_1500("14:00-15:00"),
        SLOT_1500_1600("15:00-16:00"),
        SLOT_1600_1700("16:00-17:00");

        private final String displayName;

        TimeSlot(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Status {
        PENDING_CONFIRMATION("待确认", "warning"),
        CONFIRMED("已确认", "info"),
        COMPLETED("已完成", "success"),
        CANCELED("已取消", "danger");
        
        private final String displayName;
        private final String style;
        
        Status(String displayName, String style) {
            this.displayName = displayName;
            this.style = style;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getStyle() {
            return style;
        }
    }

    @NotNull
    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "time_slot")
    private TimeSlot timeSlot;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "vet_id")
    private Vet vet;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    @Nullable
    private String notes;

    @Version
    private Integer version;

    // Getters and setters
    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public Vet getVet() {
        return vet;
    }

    public void setVet(Vet vet) {
        this.vet = vet;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Nullable
    public String getNotes() {
        return notes;
    }

    public void setNotes(@Nullable String notes) {
        this.notes = notes;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
    
    // 时区转换方法
    public ZonedDateTime getAppointmentDateTime(ZoneId zoneId) {
        // 默认使用UTC时区
        ZoneId utcZone = ZoneId.of("UTC");
        
        // 根据时间段确定具体时间
        String[] timeRange = this.timeSlot.getDisplayName().split("-");
        String startTime = timeRange[0];
        String[] hourMinute = startTime.split(":");
        int hour = Integer.parseInt(hourMinute[0]);
        int minute = Integer.parseInt(hourMinute[1]);
        
        // 创建UTC时间
        ZonedDateTime utcDateTime = ZonedDateTime.of(
            this.appointmentDate.getYear(),
            this.appointmentDate.getMonthValue(),
            this.appointmentDate.getDayOfMonth(),
            hour,
            minute,
            0,
            0,
            utcZone
        );
        
        // 转换为指定时区
        return utcDateTime.withZoneSameInstant(zoneId);
    }
}