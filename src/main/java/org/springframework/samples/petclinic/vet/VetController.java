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
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
class VetController {

	private final VetRepository vetRepository;
	private final AppointmentService appointmentService;

	public VetController(VetRepository vetRepository, AppointmentService appointmentService) {
		this.vetRepository = vetRepository;
		this.appointmentService = appointmentService;
	}

	@GetMapping("/vets.html")
	public String showVetList(@RequestParam(defaultValue = "1") int page, Model model) {
		// Here we are returning an object of type 'Vets' rather than a collection of Vet
		// objects so it is simpler for Object-Xml mapping
		Vets vets = new Vets();
		Page<Vet> paginated = findPaginated(page);
		vets.getVetList().addAll(paginated.toList());
		return addPaginationModel(page, paginated, model);
	}

	private String addPaginationModel(int page, Page<Vet> paginated, Model model) {
		List<Vet> listVets = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listVets", listVets);
		return "vets/vetList";
	}

	private Page<Vet> findPaginated(int page) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return vetRepository.findAll(pageable);
	}

	@GetMapping({ "/vets" })
	public @ResponseBody Vets showResourcesVetList() {
		// Here we are returning an object of type 'Vets' rather than a collection of Vet
		// objects so it is simpler for JSon/Object mapping
		Vets vets = new Vets();
		vets.getVetList().addAll(this.vetRepository.findAll());
		return vets;
	}
	
	@GetMapping("/vets/{vetId}/schedule")
	public String showVetSchedule(@PathVariable("vetId") Integer vetId,
			@RequestParam(value = "date", required = false) LocalDate date,
			Model model) {
		// 获取兽医信息
		Vet vet = vetRepository.findById(vetId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid vet Id: " + vetId));
		model.addAttribute("vet", vet);
		
		// 如果没有提供日期，则使用当前日期
		if (date == null) {
			date = LocalDate.now();
		}
		
		// 计算当周的开始和结束日期（周一到周日）
		WeekFields weekFields = WeekFields.of(Locale.getDefault());
		LocalDate weekStart = date.with(weekFields.dayOfWeek(), 1);
		LocalDate weekEnd = date.with(weekFields.dayOfWeek(), 7);
		
		// 获取该兽医在当周的所有预约
		List<Appointment> appointments = appointmentService.getAppointmentsByVetAndWeek(vetId, weekStart, weekEnd);
		model.addAttribute("appointments", appointments);
		
		// 统计预约总数、已完成和已取消的预约数
		long totalAppointments = appointments.size();
		long completedAppointments = appointments.stream()
				.filter(a -> a.getStatus() == Appointment.Status.COMPLETED)
				.count();
		long canceledAppointments = appointments.stream()
				.filter(a -> a.getStatus() == Appointment.Status.CANCELED)
				.count();
		
		model.addAttribute("totalAppointments", totalAppointments);
		model.addAttribute("completedAppointments", completedAppointments);
		model.addAttribute("canceledAppointments", canceledAppointments);
		model.addAttribute("currentDate", date);
		model.addAttribute("weekStart", weekStart);
		model.addAttribute("weekEnd", weekEnd);
		
		// 添加上下周的日期用于切换
		model.addAttribute("previousWeekDate", weekStart.minusDays(1));
		model.addAttribute("nextWeekDate", weekEnd.plusDays(1));
		
		return "vets/vetSchedule";
	}

}
