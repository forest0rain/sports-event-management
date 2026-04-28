package com.sports.platform.controller;

import com.sports.platform.entity.Event;
import com.sports.platform.entity.Schedule;
import com.sports.platform.entity.SportType;
import com.sports.platform.repository.EventRepository;
import com.sports.platform.repository.ScheduleRepository;
import com.sports.platform.repository.SportTypeRepository;
import com.sports.platform.repository.VenueRepository;
import com.sports.platform.service.ScheduleGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 智能赛程编排控制器
 */
@Controller
@RequestMapping("/schedule-generation")
@RequiredArgsConstructor
@Slf4j
public class ScheduleGenerationController {

    private final ScheduleGenerationService scheduleGenerationService;
    private final EventRepository eventRepository;
    private final ScheduleRepository scheduleRepository;
    private final SportTypeRepository sportTypeRepository;
    private final VenueRepository venueRepository;

    /**
     * 赛程编排页面
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public String scheduleGenerationPage(Model model) {
        // 获取所有已发布的赛事（分页获取全部）
        List<Event> events = eventRepository.findByStatus("PUBLISHED", PageRequest.of(0, 100)).getContent();
        model.addAttribute("events", events);
        return "admin/schedule-generation";
    }

    /**
     * 获取赛事的赛程列表
     */
    @GetMapping("/schedules/{eventId}")
    @ResponseBody
    public ResponseEntity<List<ScheduleDTO>> getEventSchedules(@PathVariable Long eventId) {
        List<Schedule> schedules = scheduleRepository.findByEventId(eventId);

        List<ScheduleDTO> dtos = schedules.stream().map(s -> {
            ScheduleDTO dto = new ScheduleDTO();
            dto.setId(s.getId());
            dto.setEventId(s.getEvent() != null ? s.getEvent().getId() : null);
            dto.setSportTypeId(s.getSportType() != null ? s.getSportType().getId() : null);
            dto.setSportTypeName(s.getSportType() != null ? s.getSportType().getName() : "未指定");
            // 使用date字段（与前端一致）
            dto.setDate(s.getDate());
            dto.setStartTime(s.getStartTime());
            dto.setEndTime(s.getEndTime());
            dto.setVenueId(s.getVenue() != null ? s.getVenue().getId() : null);
            dto.setVenueName(s.getVenue() != null ? s.getVenue().getName() : "未分配");
            dto.setStatus(s.getStatus());
            // 使用roundType字段（与前端一致）
            dto.setRoundType(s.getRoundType());
            dto.setGroupName(s.getGroupName());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * 执行智能编排
     */
    @PostMapping("/generate/{eventId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateSchedule(@PathVariable Long eventId) {
        log.info("接收编排请求, eventId: {}", eventId);

        ScheduleGenerationService.GenerationResult result =
            scheduleGenerationService.generateSchedule(eventId);

        Map<String, Object> response = new HashMap<>();
        if (result.isSuccess()) {
            response.put("success", true);
            response.put("message", result.getMessage());
            response.put("optimizedCount", result.getOptimizedCount());
            response.put("score", result.getScore());
            response.put("conflicts", result.getConflicts());
        } else {
            response.put("success", false);
            response.put("message", result.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取编排统计信息
     */
    @GetMapping("/statistics/{eventId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatistics(@PathVariable Long eventId) {
        List<Schedule> schedules = scheduleRepository.findByEventId(eventId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSchedules", schedules.size());
        stats.put("scheduledCount", schedules.stream()
            .filter(s -> "SCHEDULED".equals(s.getStatus())).count());
        stats.put("pendingCount", schedules.stream()
            .filter(s -> "PENDING".equals(s.getStatus())).count());

        // 按运动项目统计
        Map<Long, Long> bySport = schedules.stream()
            .collect(Collectors.groupingBy(s -> s.getSportType() != null ? s.getSportType().getId() : 0L, Collectors.counting()));
        stats.put("bySportType", bySport);

        // 按日期统计
        long scheduledDays = schedules.stream()
            .map(Schedule::getDate)
            .filter(d -> d != null)
            .distinct()
            .count();
        stats.put("scheduledDays", scheduledDays);

        return ResponseEntity.ok(stats);
    }

    // ============ DTO ============

    @lombok.Data
    public static class ScheduleDTO {
        private Long id;
        private Long eventId;
        private Long sportTypeId;
        private String sportTypeName;
        private java.time.LocalDate date;
        private java.time.LocalTime startTime;
        private java.time.LocalTime endTime;
        private Long venueId;
        private String venueName;
        private String status;
        private String roundType;
        private String groupName;
    }
}
