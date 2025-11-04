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
 * @author Trae AI
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    /**
     * 查找指定兽医在指定日期的所有预约
     *
     * @param vetId 兽医ID
     * @param date 预约日期
     * @return 预约列表
     */
    List<Appointment> findByVetIdAndAppointmentDate(Integer vetId, LocalDate date);
    
    /**
     * 查找指定日期的所有预约
     *
     * @param date 预约日期
     * @return 预约列表
     */
    List<Appointment> findByAppointmentDate(LocalDate date);
    
    /**
     * 查找指定主人的所有预约
     *
     * @param ownerId 主人ID
     * @return 预约列表
     */
    @Query("SELECT a FROM Appointment a WHERE a.pet.owner.id = :ownerId")
    List<Appointment> findByPetOwnerId(@Param("ownerId") Integer ownerId);
    
    /**
     * 查找指定兽医在指定日期和时间段的预约
     * 用于检测预约冲突
     *
     * @param vetId 兽医ID
     * @param date 预约日期
     * @param timeSlot 时间段
     * @return 预约信息
     */
    Optional<Appointment> findByVetIdAndAppointmentDateAndTimeSlot(Integer vetId, LocalDate date, Appointment.TimeSlot timeSlot);
    
    /**
     * 查找指定兽医、宠物、日期和时间段的预约
     * 用于幂等保障
     *
     * @param vetId 兽医ID
     * @param petId 宠物ID
     * @param date 预约日期
     * @param timeSlot 时间段
     * @return 预约信息
     */
    Optional<Appointment> findByVetIdAndPetIdAndAppointmentDateAndTimeSlot(
        Integer vetId, Integer petId, LocalDate date, Appointment.TimeSlot timeSlot);
    
    /**
     * 查找指定兽医在指定周的所有预约
     *
     * @param vetId 兽医ID
     * @param startOfWeek 周开始日期
     * @param endOfWeek 周结束日期
     * @return 预约列表
     */
    @Query("SELECT a FROM Appointment a WHERE a.vet.id = :vetId AND a.appointmentDate BETWEEN :startOfWeek AND :endOfWeek")
    List<Appointment> findByVetIdAndWeek(@Param("vetId") Integer vetId, 
                                       @Param("startOfWeek") LocalDate startOfWeek, 
                                       @Param("endOfWeek") LocalDate endOfWeek);
    
    /**
     * 统计指定兽医在指定周的预约数量
     *
     * @param vetId 兽医ID
     * @param startOfWeek 周开始日期
     * @param endOfWeek 周结束日期
     * @return 预约数量
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.vet.id = :vetId AND a.appointmentDate BETWEEN :startOfWeek AND :endOfWeek")
    Long countByVetIdAndWeek(@Param("vetId") Integer vetId, 
                            @Param("startOfWeek") LocalDate startOfWeek, 
                            @Param("endOfWeek") LocalDate endOfWeek);
    
    /**
     * 统计指定兽医在指定周的已完成预约数量
     *
     * @param vetId 兽医ID
     * @param startOfWeek 周开始日期
     * @param endOfWeek 周结束日期
     * @return 已完成预约数量
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.vet.id = :vetId AND a.appointmentDate BETWEEN :startOfWeek AND :endOfWeek AND a.status = 'COMPLETED'")
    Long countCompletedByVetIdAndWeek(@Param("vetId") Integer vetId, 
                                     @Param("startOfWeek") LocalDate startOfWeek, 
                                     @Param("endOfWeek") LocalDate endOfWeek);
    
    /**
     * 统计指定兽医在指定周的已取消预约数量
     *
     * @param vetId 兽医ID
     * @param startOfWeek 周开始日期
     * @param endOfWeek 周结束日期
     * @return 已取消预约数量
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.vet.id = :vetId AND a.appointmentDate BETWEEN :startOfWeek AND :endOfWeek AND a.status = 'CANCELED'")
    Long countCancelledByVetIdAndWeek(@Param("vetId") Integer vetId, 
                                     @Param("startOfWeek") LocalDate startOfWeek, 
                                     @Param("endOfWeek") LocalDate endOfWeek);
    
    /**
     * 查找所有在指定时间前24小时内的预约
     * 用于发送提醒
     *
     * @param currentDateTime 当前时间
     * @param reminderTime 提醒时间（当前时间+24小时）
     * @return 预约列表
     */
    @Query("SELECT a FROM Appointment a WHERE a.status IN ('PENDING_CONFIRMATION', 'CONFIRMED') AND 
           FUNCTION('DATE_ADD', a.appointmentDate, FUNCTION('HOUR', SUBSTRING_INDEX(a.timeSlot, '-', 1))) BETWEEN :currentDateTime AND :reminderTime")
    List<Appointment> findAppointmentsNeedingReminder(@Param("currentDateTime") java.util.Date currentDateTime, 
                                                     @Param("reminderTime") java.util.Date reminderTime);
}