package com.sports.platform.controller;

import com.sports.platform.entity.Result;
import com.sports.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 数据统计控制器
 * 提供各类统计数据接口供前端图表使用
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final EventRepository eventRepository;
    private final AthleteRepository athleteRepository;
    private final RegistrationRepository registrationRepository;
    private final ResultRepository resultRepository;
    private final ScheduleRepository scheduleRepository;

    /**
     * 获取所有统计数据
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 基本统计
        stats.put("totalEvents", eventRepository.count());
        stats.put("totalAthletes", athleteRepository.count());
        stats.put("totalRegistrations", registrationRepository.count());
        stats.put("totalResults", resultRepository.count());
        
        // 赛事状态分布
        stats.put("eventStatusData", eventRepository.countByStatus());
        
        // 运动员性别分布
        stats.put("athleteGenderData", athleteRepository.countByGender());
        
        // 运动员年龄段分布
        stats.put("athleteAgeData", athleteRepository.countByAgeGroup());
        
        // 成绩状态分布
        stats.put("resultStatusData", resultRepository.countByResultStatus());
        
        // Top运动员
        stats.put("topAthletesData", resultRepository.countAwardsByAthletes());
        
        // 赛事报名情况
        stats.put("eventRegistrationData", getEventRegistrationData());
        
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取赛事状态统计数据
     */
    @GetMapping("/events/status")
    public ResponseEntity<List<Object[]>> getEventStatusStats() {
        return ResponseEntity.ok(eventRepository.countByStatus());
    }

    /**
     * 获取运动员统计数据
     */
    @GetMapping("/athletes")
    public ResponseEntity<Map<String, Object>> getAthleteStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", athleteRepository.count());
        stats.put("byGender", athleteRepository.countByGender());
        stats.put("byAgeGroup", athleteRepository.countByAgeGroup());
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取成绩统计数据
     */
    @GetMapping("/results")
    public ResponseEntity<Map<String, Object>> getResultStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", resultRepository.count());
        stats.put("byStatus", resultRepository.countByResultStatus());
        stats.put("topAthletes", resultRepository.countAwardsByAthletes());
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取破纪录统计
     */
    @GetMapping("/records")
    public ResponseEntity<List<Object[]>> getRecordStats() {
        return ResponseEntity.ok(resultRepository.countByRecordType());
    }

    /**
     * 获取赛事报名数据
     */
    private List<Object[]> getEventRegistrationData() {
        List<Object[]> data = new ArrayList<>();
        eventRepository.findAll(PageRequest.of(0, 10)).getContent().forEach(event -> {
            Long currentCount = registrationRepository.countByEventIdAndStatus(event.getId(), "APPROVED");
            data.add(new Object[]{event.getName(), event.getMaxParticipants(), currentCount});
        });
        return data;
    }
}
