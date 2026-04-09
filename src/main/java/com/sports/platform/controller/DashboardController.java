package com.sports.platform.controller;

import com.sports.platform.entity.Event;
import com.sports.platform.entity.RoleApplication;
import com.sports.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘控制器
 */
@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final EventRepository eventRepository;
    private final AthleteRepository athleteRepository;
    private final RegistrationRepository registrationRepository;
    private final ScheduleRepository scheduleRepository;
    private final ResultRepository resultRepository;
    private final RoleApplicationRepository roleApplicationRepository;

    /**
     * 首页/仪表盘
     */
    @GetMapping
    public String index(Model model) {
        // 统计各模块数量
        long totalEvents = eventRepository.count();
        long totalAthletes = athleteRepository.count();
        long totalSchedules = scheduleRepository.count();
        long totalResults = resultRepository.count();
        
        // 待审核报名数量
        long pendingRegistrations = registrationRepository.findByStatus("PENDING", PageRequest.of(0, 1))
                .map(page -> page.getTotalElements())
                .orElse(0L);
        
        // 待审核角色申请数量（仅管理员）
        long pendingCount = roleApplicationRepository.countByStatus("PENDING");
        
        // 获取进行中赛事数量
        List<Event> ongoingEvents = eventRepository.findByStatus("ONGOING", PageRequest.of(0, 10)).getContent();
        
        // 获取报名中赛事
        List<Event> registrationEvents = eventRepository.findByStatus("REGISTRATION", PageRequest.of(0, 5)).getContent();
        
        // 统计数据
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvents", totalEvents);
        stats.put("totalAthletes", totalAthletes);
        stats.put("totalSchedules", totalSchedules);
        stats.put("totalResults", totalResults);
        stats.put("pendingRegistrations", pendingRegistrations);
        stats.put("ongoingEventsCount", ongoingEvents.size());
        
        model.addAttribute("stats", stats);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("ongoingEvents", ongoingEvents);
        model.addAttribute("registrationEvents", registrationEvents);
        
        // 添加图表数据
        addChartData(model);
        
        return "dashboard/index";
    }
    
    /**
     * 添加图表数据到模型
     */
    private void addChartData(Model model) {
        // 赛事状态分布
        model.addAttribute("eventStatusData", eventRepository.countByStatus());
        
        // 运动员统计数据
        model.addAttribute("athleteCount", athleteRepository.count());
        model.addAttribute("athleteByGender", athleteRepository.countByGender());
        model.addAttribute("athleteByAgeGroup", athleteRepository.countByAgeGroup());
        
        // 成绩统计
        model.addAttribute("resultCount", resultRepository.count());
        model.addAttribute("resultByStatus", resultRepository.countByResultStatus());
        
        // Top运动员
        model.addAttribute("topAthletes", resultRepository.countAwardsByAthletes());
    }
}
