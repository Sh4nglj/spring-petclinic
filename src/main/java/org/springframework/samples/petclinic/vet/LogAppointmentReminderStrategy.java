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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 日志预约提醒策略
 *
 * @author Trae AI
 */
@Component
public class LogAppointmentReminderStrategy implements AppointmentReminderStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(LogAppointmentReminderStrategy.class);
    
    @Override
    public void sendReminder(Appointment appointment) {
        String vetName = appointment.getVet().getFirstName() + " " + appointment.getVet().getLastName();
        String petName = appointment.getPet().getName();
        String ownerName = appointment.getPet().getOwner().getFirstName() + " " + appointment.getPet().getOwner().getLastName();
        
        logger.info("预约提醒: 兽医 {} 将于 {} {} 有一个与宠物 {} (主人: {}) 的预约。",
            vetName,
            appointment.getAppointmentDate(),
            appointment.getTimeSlot(),
            petName,
            ownerName);
        
        if (appointment.getNotes() != null && !appointment.getNotes().isEmpty()) {
            logger.info("预约备注: {}", appointment.getNotes());
        }
    }
    
    @Override
    public String getStrategyName() {
        return "log";
    }
}