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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for {@link Appointment} domain objects.
 *
 * @author Trae AI
 */
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final VetRepository vetRepository;
    private final PetRepository petRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, 
                             VetRepository vetRepository, 
                             PetRepository petRepository) {
        this.appointmentRepository = appointmentRepository;
        this.vetRepository = vetRepository;
        this.petRepository = petRepository;
    }

    /**
     * 创建新预约
     *
     * @param appointment 预约信息
     * @return 创建的预约
     * @throws IllegalArgumentException 如果预约信息无效或存在冲突
     */
    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        // 验证预约信息
        validateAppointment(appointment);
        
        // 检查是否存在冲突
        checkForConflicts(appointment);
        
        try {
            // 保存预约
            return appointmentRepository.save(appointment);
        } catch (DataIntegrityViolationException e) {
            // 数据库唯一约束冲突
            throw new IllegalArgumentException("该时段已被预约", e);
        }
    }
    
    /**
     * 创建预约（幂等操作）
     *
     * @param vetId 兽医ID
     * @param petId 宠物ID
     * @param date 预约日期
     * @param timeSlot 时间段
     * @param notes 备注
     * @return 创建或已存在的预约
     * @throws IllegalArgumentException 如果预约信息无效
     */
    @Transactional
    public Appointment createAppointmentIdempotent(Integer vetId, Integer petId, 
                                                  LocalDate date, Appointment.TimeSlot timeSlot, 
                                                  String notes) {
        // 检查预约是否已存在
        Optional<Appointment> existingAppointment = 
            appointmentRepository.findByVetIdAndPetIdAndAppointmentDateAndTimeSlot(
                vetId, petId, date, timeSlot);
        
        if (existingAppointment.isPresent()) {
            // 预约已存在，返回现有预约
            return existingAppointment.get();
        }
        
        // 预约不存在，创建新预约
        Vet vet = vetRepository.findById(vetId)
            .orElseThrow(() -> new IllegalArgumentException("无效的兽医ID"));
            
        Pet pet = petRepository.findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("无效的宠物ID"));
            
        Appointment appointment = new Appointment();
        appointment.setVet(vet);
        appointment.setPet(pet);
        appointment.setAppointmentDate(date);
        appointment.setTimeSlot(timeSlot);
        appointment.setStatus(Appointment.Status.PENDING_CONFIRMATION);
        appointment.setNotes(notes);
        
        return createAppointment(appointment);
    }

    /**
     * 更新预约信息
     *
     * @param appointment 预约信息
     * @return 更新后的预约
     * @throws IllegalArgumentException 如果预约信息无效或存在冲突
     */
    @Transactional
    public Appointment updateAppointment(Appointment appointment) {
        // 验证预约信息
        validateAppointment(appointment);
        
        // 检查是否存在冲突（排除当前预约）
        checkForConflicts(appointment, appointment.getId());
        
        try {
            // 保存预约
            return appointmentRepository.save(appointment);
        } catch (DataIntegrityViolationException e) {
            // 数据库唯一约束冲突
            throw new IllegalArgumentException("该时段已被预约", e);
        }
    }

    /**
     * 删除预约
     *
     * @param appointmentId 预约ID
     * @throws IllegalArgumentException 如果预约不存在
     */
    @Transactional
    public void deleteAppointment(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalArgumentException("无效的预约ID"));
            
        appointmentRepository.delete(appointment);
    }
    
    /**
     * 批量确认预约
     *
     * @param appointmentIds 预约ID列表
     * @return 成功确认的预约数量
     */
    @Transactional
    public int batchConfirmAppointments(List<Integer> appointmentIds) {
        int confirmedCount = 0;
        
        for (Integer id : appointmentIds) {
            Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
            if (optionalAppointment.isPresent()) {
                Appointment appointment = optionalAppointment.get();
                if (appointment.getStatus() == Appointment.Status.PENDING_CONFIRMATION) {
                    appointment.setStatus(Appointment.Status.CONFIRMED);
                    appointmentRepository.save(appointment);
                    confirmedCount++;
                }
            }
        }
        
        return confirmedCount;
    }
    
    /**
     * 批量取消预约
     *
     * @param appointmentIds 预约ID列表
     * @return 成功取消的预约数量
     */
    @Transactional
    public int batchCancelAppointments(List<Integer> appointmentIds) {
        int cancelledCount = 0;
        
        for (Integer id : appointmentIds) {
            Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
            if (optionalAppointment.isPresent()) {
                Appointment appointment = optionalAppointment.get();
                if (appointment.getStatus() == Appointment.Status.PENDING_CONFIRMATION || 
                    appointment.getStatus() == Appointment.Status.CONFIRMED) {
                    appointment.setStatus(Appointment.Status.CANCELED);
                    appointmentRepository.save(appointment);
                    cancelledCount++;
                }
            }
        }
        
        return cancelledCount;
    }

    /**
     * 查找指定兽医在指定日期的所有预约
     *
     * @param vetId 兽医ID
     * @param date 预约日期
     * @return 预约列表
     */
    @Transactional(readOnly = true)
    public List<Appointment> findAppointmentsByVetAndDate(Integer vetId, LocalDate date) {
        return appointmentRepository.findByVetIdAndAppointmentDate(vetId, date);
    }
    
    /**
     * 查找指定主人的所有预约
     *
     * @param ownerId 主人ID
     * @return 预约列表
     */
    @Transactional(readOnly = true)
    public List<Appointment> findAppointmentsByOwnerId(Integer ownerId) {
        return appointmentRepository.findByPetOwnerId(ownerId);
    }
    
    /**
     * 查找今天的所有预约
     *
     * @return 预约列表
     */
    @Transactional(readOnly = true)
    public List<Appointment> findAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByAppointmentDate(date);
    }
    
    /**
     * 查找指定兽医在指定周的所有预约
     *
     * @param vetId 兽医ID
     * @param startOfWeek 周开始日期
     * @param endOfWeek 周结束日期
     * @return 预约列表
     */
    @Transactional(readOnly = true)
    public List<Appointment> findAppointmentsByVetAndWeek(Integer vetId, 
                                                         LocalDate startOfWeek, 
                                                         LocalDate endOfWeek) {
        return appointmentRepository.findByVetIdAndWeek(vetId, startOfWeek, endOfWeek);
    }
    
    /**
     * 获取指定兽医在指定日期的可用时间段
     *
     * @param vetId 兽医ID
     * @param date 预约日期
     * @return 可用时间段列表
     */
    @Transactional(readOnly = true)
    public List<Appointment.TimeSlot> getAvailableTimeSlots(Integer vetId, LocalDate date) {
        // 获取所有可能的时间段
        List<Appointment.TimeSlot> allTimeSlots = List.of(Appointment.TimeSlot.values());
        
        // 获取已预约的时间段
        List<Appointment> appointments = appointmentRepository.findByVetIdAndAppointmentDate(vetId, date);
        List<Appointment.TimeSlot> bookedTimeSlots = appointments.stream()
            .map(Appointment::getTimeSlot)
            .collect(Collectors.toList());
        
        // 过滤可用时间段（排除已预约的）
        return allTimeSlots.stream()
            .filter(timeSlot -> !bookedTimeSlots.contains(timeSlot))
            .collect(Collectors.toList());
    }
    
    /**
     * 获取指定兽医在指定周的预约统计
     *
     * @param vetId 兽医ID
     * @param startOfWeek 周开始日期
     * @param endOfWeek 周结束日期
     * @return 预约统计信息
     */
    @Transactional(readOnly = true)
    public AppointmentStats getAppointmentStats(Integer vetId, 
                                               LocalDate startOfWeek, 
                                               LocalDate endOfWeek) {
        long total = appointmentRepository.countByVetIdAndWeek(vetId, startOfWeek, endOfWeek);
        long completed = appointmentRepository.countCompletedByVetIdAndWeek(vetId, startOfWeek, endOfWeek);
        long cancelled = appointmentRepository.countCancelledByVetIdAndWeek(vetId, startOfWeek, endOfWeek);
        
        return new AppointmentStats(total, completed, cancelled);
    }
    
    /**
     * 验证预约信息
     *
     * @param appointment 预约信息
     * @throws IllegalArgumentException 如果预约信息无效
     */
    private void validateAppointment(Appointment appointment) {
        if (appointment.getVet() == null || appointment.getVet().getId() == null) {
            throw new IllegalArgumentException("必须指定兽医");
        }
        
        if (appointment.getPet() == null || appointment.getPet().getId() == null) {
            throw new IllegalArgumentException("必须指定宠物");
        }
        
        if (appointment.getAppointmentDate() == null) {
            throw new IllegalArgumentException("必须指定预约日期");
        }
        
        if (appointment.getTimeSlot() == null) {
            throw new IllegalArgumentException("必须指定预约时间段");
        }
        
        if (appointment.getStatus() == null) {
            throw new IllegalArgumentException("必须指定预约状态");
        }
        
        // 验证日期不能是过去的日期
        LocalDate today = LocalDate.now();
        if (appointment.getAppointmentDate().isBefore(today)) {
            throw new IllegalArgumentException("预约日期不能是过去的日期");
        }
        
        // 验证兽医是否存在
        vetRepository.findById(appointment.getVet().getId())
            .orElseThrow(() -> new IllegalArgumentException("无效的兽医ID"));
            
        // 验证宠物是否存在
        petRepository.findById(appointment.getPet().getId())
            .orElseThrow(() -> new IllegalArgumentException("无效的宠物ID"));
    }
    
    /**
     * 检查是否存在预约冲突
     *
     * @param appointment 预约信息
     * @throws IllegalArgumentException 如果存在冲突
     */
    private void checkForConflicts(Appointment appointment) {
        checkForConflicts(appointment, null);
    }
    
    /**
     * 检查是否存在预约冲突（排除指定预约）
     *
     * @param appointment 预约信息
     * @param excludeId 排除的预约ID
     * @throws IllegalArgumentException 如果存在冲突
     */
    private void checkForConflicts(Appointment appointment, Integer excludeId) {
        Optional<Appointment> existingAppointment = 
            appointmentRepository.findByVetIdAndAppointmentDateAndTimeSlot(
                appointment.getVet().getId(), 
                appointment.getAppointmentDate(), 
                appointment.getTimeSlot());
        
        if (existingAppointment.isPresent()) {
            Appointment existing = existingAppointment.get();
            // 如果存在相同的预约且不是当前预约，则存在冲突
            if (!existing.getId().equals(excludeId)) {
                throw new IllegalArgumentException("该时段已被预约");
            }
        }
    }
    
    /**
     * 预约统计信息类
     */
    public static class AppointmentStats {
        private final long total;
        private final long completed;
        private final long cancelled;
        
        public AppointmentStats(long total, long completed, long cancelled) {
            this.total = total;
            this.completed = completed;
            this.cancelled = cancelled;
        }
        
        public long getTotal() {
            return total;
        }
        
        public long getCompleted() {
            return completed;
        }
        
        public long getCancelled() {
            return cancelled;
        }
        
        public long getPending() {
            return total - completed - cancelled;
        }
    }
}