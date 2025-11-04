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

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;

/**
 * Controller class for {@link Appointment} domain objects.
 *
 * @author Trae AI
 */
@Controller
@RequestMapping("/appointments")
class AppointmentController {

    private static final String VIEWS_APPOINTMENTS_CREATE_OR_UPDATE_FORM = "appointments/createOrUpdateAppointmentForm";
    private static final String VIEWS_APPOINTMENTS_LIST = "appointments/appointmentsList";
    
    private final AppointmentService appointmentService;
    private final VetRepository vetRepository;
    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;

    public AppointmentController(AppointmentService appointmentService, 
                                VetRepository vetRepository, 
                                PetRepository petRepository, 
                                OwnerRepository ownerRepository) {
        this.appointmentService = appointmentService;
        this.vetRepository = vetRepository;
        this.petRepository = petRepository;
        this.ownerRepository = ownerRepository;
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }
    
    @ModelAttribute("allVets")
    public List<Vet> populateVets() {
        return vetRepository.findAll();
    }
    
    @ModelAttribute("allTimeSlots")
    public List<Appointment.TimeSlot> populateTimeSlots() {
        return List.of(Appointment.TimeSlot.values());
    }
    
    @ModelAttribute("allStatuses")
    public List<Appointment.Status> populateStatuses() {
        return List.of(Appointment.Status.values());
    }
    
    @GetMapping("/new")
    public String initCreationForm(@RequestParam("ownerId") Integer ownerId, 
                                  @RequestParam("petId") Integer petId, 
                                  Model model) {
        // 获取宠物和主人信息
        Pet pet = petRepository.findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("无效的宠物ID: " + petId));
            
        Owner owner = ownerRepository.findById(ownerId)
            .orElseThrow(() -> new IllegalArgumentException("无效的主人ID: " + ownerId));
            
        // 创建新预约并关联宠物和主人
        Appointment appointment = new Appointment();
        appointment.setPet(pet);
        appointment.setStatus(Appointment.Status.PENDING_CONFIRMATION);
        
        model.addAttribute("appointment", appointment);
        model.addAttribute("pet", pet);
        model.addAttribute("owner", owner);
        
        // 设置默认日期为明天
        model.addAttribute("defaultDate", LocalDate.now().plusDays(1));
        
        return VIEWS_APPOINTMENTS_CREATE_OR_UPDATE_FORM;
    }
    
    @PostMapping("/new")
    public String processCreationForm(@Valid Appointment appointment, 
                                     BindingResult result, 
                                     Model model, 
                                     RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            // 获取相关的宠物和主人信息
            Pet pet = appointment.getPet();
            Owner owner = ownerRepository.findById(pet.getOwner().getId())
                .orElseThrow(() -> new IllegalArgumentException("无效的主人ID: " + pet.getOwner().getId()));
                
            model.addAttribute("pet", pet);
            model.addAttribute("owner", owner);
            return VIEWS_APPOINTMENTS_CREATE_OR_UPDATE_FORM;
        }
        
        try {
            // 创建预约
            appointmentService.createAppointment(appointment);
            redirectAttributes.addFlashAttribute("message", "预约已创建");
        } catch (IllegalArgumentException e) {
            result.rejectValue("timeSlot", "conflict", e.getMessage());
            
            // 获取相关的宠物和主人信息
            Pet pet = appointment.getPet();
            Owner owner = ownerRepository.findById(pet.getOwner().getId())
                .orElseThrow(() -> new IllegalArgumentException("无效的主人ID: " + pet.getOwner().getId()));
                
            model.addAttribute("pet", pet);
            model.addAttribute("owner", owner);
            return VIEWS_APPOINTMENTS_CREATE_OR_UPDATE_FORM;
        }
        
        // 重定向到宠物主详情页
        return "redirect:/owners/" + appointment.getPet().getOwner().getId();
    }
    
    @GetMapping("/{appointmentId}/edit")
    public String initUpdateForm(@PathVariable("appointmentId") Integer appointmentId, Model model) {
        // 获取预约信息
        Appointment appointment = appointmentService.findById(appointmentId)
            .orElseThrow(() -> new IllegalArgumentException("无效的预约ID: " + appointmentId));
            
        // 获取相关的宠物和主人信息
        Pet pet = appointment.getPet();
        Owner owner = ownerRepository.findById(pet.getOwner().getId())
            .orElseThrow(() -> new IllegalArgumentException("无效的主人ID: " + pet.getOwner().getId()));
            
        model.addAttribute("appointment", appointment);
        model.addAttribute("pet", pet);
        model.addAttribute("owner", owner);
        
        return VIEWS_APPOINTMENTS_CREATE_OR_UPDATE_FORM;
    }
    
    @PostMapping("/{appointmentId}/edit")
    public String processUpdateForm(@Valid Appointment appointment, 
                                  BindingResult result, 
                                  @PathVariable("appointmentId") Integer appointmentId, 
                                  Model model, 
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            // 获取相关的宠物和主人信息
            Pet pet = appointment.getPet();
            Owner owner = ownerRepository.findById(pet.getOwner().getId())
                .orElseThrow(() -> new IllegalArgumentException("无效的主人ID: " + pet.getOwner().getId()));
                
            model.addAttribute("pet", pet);
            model.addAttribute("owner", owner);
            return VIEWS_APPOINTMENTS_CREATE_OR_UPDATE_FORM;
        }
        
        // 设置预约ID（从路径变量中获取）
        appointment.setId(appointmentId);
        
        try {
            // 更新预约
            appointmentService.updateAppointment(appointment);
            redirectAttributes.addFlashAttribute("message", "预约已更新");
        } catch (IllegalArgumentException e) {
            result.rejectValue("timeSlot", "conflict", e.getMessage());
            
            // 获取相关的宠物和主人信息
            Pet pet = appointment.getPet();
            Owner owner = ownerRepository.findById(pet.getOwner().getId())
                .orElseThrow(() -> new IllegalArgumentException("无效的主人ID: " + pet.getOwner().getId()));
                
            model.addAttribute("pet", pet);
            model.addAttribute("owner", owner);
            return VIEWS_APPOINTMENTS_CREATE_OR_UPDATE_FORM;
        }
        
        // 重定向到宠物主详情页
        return "redirect:/owners/" + appointment.getPet().getOwner().getId();
    }
    
    @GetMapping("/{appointmentId}/delete")
    public String processDeleteForm(@PathVariable("appointmentId") Integer appointmentId, 
                                   RedirectAttributes redirectAttributes) {
        try {
            // 删除预约
            appointmentService.deleteAppointment(appointmentId);
            redirectAttributes.addFlashAttribute("message", "预约已删除");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        // 重定向到预约列表页
        return "redirect:/appointments";
    }
    
    @GetMapping
    public String showAppointmentsList(@RequestParam(value = "vetId", required = false) Integer vetId, 
                                      @RequestParam(value = "ownerId", required = false) Integer ownerId, 
                                      Model model) {
        List<Appointment> appointments;
        
        if (vetId != null) {
            // 根据兽医ID筛选预约
            Vet vet = vetRepository.findById(vetId)
                .orElseThrow(() -> new IllegalArgumentException("无效的兽医ID: " + vetId));
            appointments = appointmentService.findAppointmentsByVetAndDate(vetId, LocalDate.now());
            model.addAttribute("selectedVet", vet);
        } else if (ownerId != null) {
            // 根据主人ID筛选预约
            Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("无效的主人ID: " + ownerId));
            appointments = appointmentService.findAppointmentsByOwnerId(ownerId);
            model.addAttribute("selectedOwner", owner);
        } else {
            // 显示今天的所有预约
            appointments = appointmentService.findAppointmentsByDate(LocalDate.now());
        }
        
        model.addAttribute("appointments", appointments);
        
        // 添加所有兽医和主人选项，用于筛选
        model.addAttribute("allVets", vetRepository.findAll());
        model.addAttribute("allOwners", ownerRepository.findAll());
        
        return VIEWS_APPOINTMENTS_LIST;
    }
}