package com.sports.platform.controller;

import com.sports.platform.entity.Schedule;
import com.sports.platform.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 赛程管理控制器
 */
@Controller
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * 赛程列表
     */
    @GetMapping
    public String list(@RequestParam(required = false) Long eventId,
                      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                      Model model) {
        List<Schedule> schedules;
        
        if (eventId != null) {
            schedules = scheduleService.getSchedulesByEvent(eventId);
        } else if (date != null) {
            schedules = scheduleService.getSchedulesByDate(date);
        } else {
            // 默认显示今天的赛程
            schedules = scheduleService.getSchedulesByDate(LocalDate.now());
        }

        model.addAttribute("schedules", schedules);
        model.addAttribute("eventId", eventId);
        model.addAttribute("selectedDate", date != null ? date : LocalDate.now());
        
        return "schedule/list";
    }

    /**
     * 赛程详情
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Schedule schedule = scheduleService.getScheduleById(id);
        model.addAttribute("schedule", schedule);
        return "schedule/detail";
    }

    /**
     * 智能编排页面
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    @GetMapping("/generate")
    public String generatePage(@RequestParam Long eventId, Model model) {
        model.addAttribute("eventId", eventId);
        return "schedule/generate";
    }

    /**
     * 智能编排赛程
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    @PostMapping("/generate")
    public String generate(@RequestParam Long eventId,
                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                          @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime dailyStartTime,
                          @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime dailyEndTime,
                          RedirectAttributes redirectAttributes) {
        try {
            List<Schedule> schedules = scheduleService.generateScheduleSmart(
                    eventId, startDate, endDate, dailyStartTime, dailyEndTime);
            redirectAttributes.addFlashAttribute("success", 
                    String.format("赛程编排成功，共生成 %d 场比赛", schedules.size()));
            return "redirect:/schedules?eventId=" + eventId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/schedules/generate?eventId=" + eventId;
        }
    }

    /**
     * 创建赛程页面
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    @GetMapping("/create")
    public String createPage(@RequestParam(required = false) Long eventId, Model model) {
        Schedule schedule = new Schedule();
        if (eventId != null) {
            // 设置默认值
        }
        model.addAttribute("schedule", schedule);
        model.addAttribute("eventId", eventId);
        return "schedule/form";
    }

    /**
     * 创建赛程
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    @PostMapping("/create")
    public String create(@ModelAttribute Schedule schedule,
                        RedirectAttributes redirectAttributes) {
        try {
            scheduleService.createSchedule(schedule);
            redirectAttributes.addFlashAttribute("success", "赛程创建成功");
            return "redirect:/schedules/" + schedule.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/schedules/create";
        }
    }

    /**
     * 编辑赛程页面
     */
    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, Model model) {
        Schedule schedule = scheduleService.getScheduleById(id);
        model.addAttribute("schedule", schedule);
        return "schedule/form";
    }

    /**
     * 更新赛程
     */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                        @ModelAttribute Schedule schedule,
                        RedirectAttributes redirectAttributes) {
        try {
            scheduleService.updateSchedule(id, schedule);
            redirectAttributes.addFlashAttribute("success", "赛程更新成功");
            return "redirect:/schedules/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/schedules/" + id + "/edit";
        }
    }

    /**
     * 更新赛程状态
     */
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                              @RequestParam String status,
                              RedirectAttributes redirectAttributes) {
        try {
            scheduleService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "状态更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/schedules/" + id;
    }

    /**
     * 删除赛程
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            scheduleService.deleteSchedule(id);
            redirectAttributes.addFlashAttribute("success", "赛程已删除");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/schedules";
    }

    /**
     * 赛程日历视图
     */
    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) Long eventId, Model model) {
        if (eventId != null) {
            List<LocalDate> dates = scheduleService.getEventDates(eventId);
            model.addAttribute("eventDates", dates);
        }
        
        model.addAttribute("eventId", eventId);
        return "schedule/calendar";
    }
}
