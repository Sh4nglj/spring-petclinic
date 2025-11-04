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
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Email预约提醒策略（模拟实现）
 *
 * @author Trae AI
 */
@Component
public class EmailAppointmentReminderStrategy implements AppointmentReminderStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailAppointmentReminderStrategy.class);
    
    private final MessageSource messageSource;
    
    public EmailAppointmentReminderStrategy(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    @Override
    public void sendReminder(Appointment appointment) {
        String vetEmail = appointment.getVet().getId() + "@petclinic.example.com";
        String ownerEmail = appointment.getPet().getOwner().getEmail();
        
        String subject = messageSource.getMessage("appointment.reminder.email.subject",
            new Object[]{appointment.getPet().getName()}, Locale.getDefault());
            
        String content = messageSource.getMessage("appointment.reminder.email.content",
            new Object[]{appointment.getPet().getName(),
                appointment.getVet().getFirstName() + " " + appointment.getVet().getLastName(),
                appointment.getAppointmentDate(),
                appointment.getTimeSlot(),
                appointment.getPet().getOwner().getFirstName() + " " + appointment.getPet().getOwner().getLastName()},
            Locale.getDefault());
            
        // 模拟发送Email，实际项目中可以使用JavaMailSender等库实现真实发送
        logger.info("模拟发送Email提醒:");
        logger.info("收件人: 兽医 <{}>, 主人 <{}>", vetEmail, ownerEmail);
        logger.info("主题: {}", subject);
        logger.info("内容: {}", content);
        
        if (appointment.getNotes() != null && !appointment.getNotes().isEmpty()) {
            logger.info("备注: {}", appointment.getNotes());
        }
    }
    
    @Override
    public String getStrategyName() {
        return "email";
    }
}