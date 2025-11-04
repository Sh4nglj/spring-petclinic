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

/**
 * 预约提醒策略接口
 *
 * @author Trae AI
 */
public interface AppointmentReminderStrategy {
    
    /**
     * 发送预约提醒
     *
     * @param appointment 预约信息
     */
    void sendReminder(Appointment appointment);
    
    /**
     * 获取策略名称
     *
     * @return 策略名称
     */
    String getStrategyName();
}