package com.sports.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sports.platform.entity.Event;
import com.sports.platform.entity.RoleApplication;
import com.sports.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final ObjectMapper objectMapper;

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
        long pendingRegistrations = registrationRepository
                .findByStatus("PENDING", PageRequest.of(0, 1))
                .getTotalElements();
        
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
        
        // ========== 图表数据（合并为一个JSON对象嵌入模板） ==========
        try {
            Map<String, Object> chartData = new HashMap<>();
            chartData.put("eventStatus", toListOfLists(eventRepository.countByStatus()));
            chartData.put("athleteAge", toListOfLists(athleteRepository.countByAgeGroup()));
            model.addAttribute("chartDataJson", objectMapper.writeValueAsString(chartData));
        } catch (Exception e) {
            model.addAttribute("chartDataJson", "{}");
        }
        
        return "dashboard/index";
    }

    /**
     * 将List<Object[]>转为List<List<Object>>，确保正确序列化为JSON数组
     */
    private List<List<Object>> toListOfLists(List<Object[]> list) {
        if (list == null) return Collections.emptyList();
        return list.stream()
            .map(arr -> {
                List<Object> item = new ArrayList<>();
                if (arr != null) {
                    for (Object o : arr) {
                        item.add(o);
                    }
                }
                return item;
            })
            .collect(Collectors.toList());
    }
}
