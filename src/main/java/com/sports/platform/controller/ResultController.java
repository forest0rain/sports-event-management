package com.sports.platform.controller;

import com.sports.platform.entity.Registration;
import com.sports.platform.entity.Result;
import com.sports.platform.repository.*;
import com.sports.platform.service.RegistrationService;
import com.sports.platform.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 成绩管理控制器
 */
@Controller
@RequestMapping("/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;
    private final RegistrationService registrationService;
    private final EventRepository eventRepository;
    private final AthleteRepository athleteRepository;
    private final RegistrationRepository registrationRepository;
    private final ResultRepository resultRepository;
    private final ScheduleRepository scheduleRepository;

    /**
     * 成绩列表
     */
    @GetMapping
    public String list(@RequestParam(required = false) Long scheduleId,
                      @RequestParam(required = false) Long athleteId,
                      Model model) {
        if (scheduleId != null) {
            List<Result> results = resultService.getResultsBySchedule(scheduleId);
            model.addAttribute("results", results);
        } else if (athleteId != null) {
            Page<Result> results = resultService.getResultsByAthlete(athleteId, 
                    PageRequest.of(0, 20, Sort.by("createdTime").descending()));
            model.addAttribute("results", results);
        }

        model.addAttribute("scheduleId", scheduleId);
        model.addAttribute("athleteId", athleteId);
        
        return "result/list";
    }

    /**
     * 成绩详情
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Result result = resultService.getResultById(id);
        model.addAttribute("result", result);
        return "result/detail";
    }

    /**
     * 录入成绩页面
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    @GetMapping("/record")
    public String recordPage(@RequestParam Long scheduleId, Model model) {
        model.addAttribute("scheduleId", scheduleId);
        model.addAttribute("statusList", List.of("VALID", "INVALID", "DNS", "DNF", "DQ"));
        return "result/form";
    }

    /**
     * 录入成绩
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    @PostMapping("/record")
    public String record(@RequestParam Long scheduleId,
                        @RequestParam Long athleteId,
                        @RequestParam BigDecimal score,
                        @RequestParam String scoreText,
                        @RequestParam String status,
                        @RequestParam(required = false) String remark,
                        RedirectAttributes redirectAttributes) {
        try {
            // 裁判ID从当前登录用户获取
            Long refereeId = 1L; // TODO: 从Security上下文获取
            
            resultService.recordResult(scheduleId, athleteId, score, scoreText, status, remark, refereeId);
            redirectAttributes.addFlashAttribute("success", "成绩录入成功");
            return "redirect:/results?scheduleId=" + scheduleId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/results/record?scheduleId=" + scheduleId;
        }
    }

    /**
     * 批量录入成绩页面
     */
    @GetMapping("/batch-record")
    public String batchRecordPage(@RequestParam Long scheduleId, Model model) {
        // 获取该赛程的已审核报名
        // List<Registration> registrations = ...;
        model.addAttribute("scheduleId", scheduleId);
        return "result/batch-form";
    }

    /**
     * 编辑成绩
     */
    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, Model model) {
        Result result = resultService.getResultById(id);
        model.addAttribute("result", result);
        model.addAttribute("statusList", List.of("VALID", "INVALID", "DNS", "DNF", "DQ"));
        return "result/form";
    }

    /**
     * 更新成绩
     */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                        @RequestParam BigDecimal score,
                        @RequestParam String scoreText,
                        @RequestParam String status,
                        @RequestParam(required = false) String remark,
                        RedirectAttributes redirectAttributes) {
        try {
            resultService.updateResult(id, score, scoreText, status, remark);
            redirectAttributes.addFlashAttribute("success", "成绩更新成功");
            return "redirect:/results/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/results/" + id + "/edit";
        }
    }

    /**
     * 删除成绩
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            resultService.deleteResult(id);
            redirectAttributes.addFlashAttribute("success", "成绩已删除");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/results";
    }

    /**
     * 排名查看
     */
    @GetMapping("/ranking")
    public String ranking(@RequestParam Long sportTypeId, Model model) {
        List<Result> ranking = resultService.getSportTypeRanking(sportTypeId);
        model.addAttribute("ranking", ranking);
        model.addAttribute("sportTypeId", sportTypeId);
        return "result/ranking";
    }

    /**
     * 数据统计页面
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    @GetMapping("/statistics")
    public String statistics(@RequestParam(required = false) Long eventId, Model model) {
        if (eventId != null) {
            ResultService.ResultStatistics stats = resultService.getStatistics(eventId);
            model.addAttribute("statistics", stats);
        }
        
        model.addAttribute("eventId", eventId);
        
        // ========== 基本统计 ==========
        model.addAttribute("totalEvents", eventRepository.count());
        model.addAttribute("totalAthletes", athleteRepository.count());
        model.addAttribute("totalRegistrations", registrationRepository.count());
        model.addAttribute("totalResults", resultRepository.count());
        
        // ========== 图表数据（JSON字符串，通过data属性传递） ==========
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            
            // 赛事状态分布
            model.addAttribute("eventStatusJson", mapper.writeValueAsString(
                toListOfLists(eventRepository.countByStatus())));
            
            // 运动员性别分布
            model.addAttribute("athleteGenderJson", mapper.writeValueAsString(
                toListOfLists(athleteRepository.countByGender())));
            
            // 运动员年龄段分布
            model.addAttribute("athleteAgeJson", mapper.writeValueAsString(
                toListOfLists(athleteRepository.countByAgeGroup())));
            
            // 成绩状态分布
            model.addAttribute("resultStatusJson", mapper.writeValueAsString(
                toListOfLists(resultRepository.countByResultStatus())));
            
            // 月度趋势
            model.addAttribute("monthlyTrendJson", mapper.writeValueAsString(
                toListOfLists(getMonthlyTrendData())));
            
            // 赛事报名情况
            model.addAttribute("eventRegistrationJson", mapper.writeValueAsString(
                toListOfLists(getEventRegistrationData())));
            
            // 破纪录统计
            List<Object[]> recordStats = resultService.getRecordStatistics();
            model.addAttribute("recordStatsJson", mapper.writeValueAsString(
                toListOfLists(recordStats)));
            // Top运动员排名（用于表格，不需要JSON）
            model.addAttribute("topAthletesData", toListOfLists(resultRepository.countAwardsByAthletes()));
            
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // 如果序列化失败，设置空数据
            model.addAttribute("eventStatusJson", "[]");
            model.addAttribute("athleteGenderJson", "[]");
            model.addAttribute("athleteAgeJson", "[]");
            model.addAttribute("resultStatusJson", "[]");
            model.addAttribute("monthlyTrendJson", "[]");
            model.addAttribute("eventRegistrationJson", "[]");
            model.addAttribute("recordStatsJson", "[]");
            model.addAttribute("topAthletesData", Collections.emptyList());
        }
        
        return "result/statistics";
    }
    
    /**
     * 将List<Object[]>转为List<List<Object>>，确保Thymeleaf正确序列化为JSON数组
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
    
    /**
     * 获取月度趋势数据
     */
    private List<Object[]> getMonthlyTrendData() {
        // 简化实现：返回近6个月的模拟数据
        // 实际项目中应根据赛事创建时间统计
        return List.of(
            new Object[]{"1月", 3, 45},
            new Object[]{"2月", 2, 38},
            new Object[]{"3月", 5, 72},
            new Object[]{"4月", 4, 61},
            new Object[]{"5月", 6, 85},
            new Object[]{"6月", 4, 53}
        );
    }
    
    /**
     * 获取赛事报名数据
     */
    private List<Object[]> getEventRegistrationData() {
        // 获取前5个赛事及其报名情况
        List<Object[]> data = new java.util.ArrayList<>();
        eventRepository.findAll(PageRequest.of(0, 5)).getContent().forEach(event -> {
            Long currentCount = registrationRepository.countByEventIdAndStatus(event.getId(), "APPROVED");
            data.add(new Object[]{event.getName(), event.getMaxParticipants(), currentCount});
        });
        return data;
    }
}
