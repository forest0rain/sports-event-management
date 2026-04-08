package com.sports.platform.controller;

import com.sports.platform.entity.Registration;
import com.sports.platform.entity.Result;
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

import java.math.BigDecimal;
import java.util.List;

/**
 * 成绩管理控制器
 */
@Controller
@RequestMapping("/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;
    private final RegistrationService registrationService;

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
    @GetMapping("/record")
    public String recordPage(@RequestParam Long scheduleId, Model model) {
        model.addAttribute("scheduleId", scheduleId);
        model.addAttribute("statusList", List.of("VALID", "INVALID", "DNS", "DNF", "DQ"));
        return "result/form";
    }

    /**
     * 录入成绩
     */
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
     * 数据可视化
     */
    @GetMapping("/statistics")
    public String statistics(@RequestParam(required = false) Long eventId, Model model) {
        if (eventId != null) {
            ResultService.ResultStatistics stats = resultService.getStatistics(eventId);
            model.addAttribute("statistics", stats);
        }
        
        // 获取破纪录统计
        List<Object[]> recordStats = resultService.getRecordStatistics();
        model.addAttribute("recordStats", recordStats);
        
        model.addAttribute("eventId", eventId);
        return "result/statistics";
    }
}
