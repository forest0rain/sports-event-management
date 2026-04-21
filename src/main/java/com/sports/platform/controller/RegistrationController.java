package com.sports.platform.controller;

import com.sports.platform.entity.Event;
import com.sports.platform.entity.Registration;
import com.sports.platform.entity.SportType;
import com.sports.platform.entity.User;
import com.sports.platform.repository.EventRepository;
import com.sports.platform.repository.RegistrationRepository;
import com.sports.platform.repository.SportTypeRepository;
import com.sports.platform.repository.UserRepository;
import com.sports.platform.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 报名管理控制器
 */
@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final SportTypeRepository sportTypeRepository;
    private final UserRepository userRepository;

    /**
     * 我的报名列表
     */
    @GetMapping("/registrations")
    public String myRegistrations(Authentication authentication, Model model) {
        Long userId = getCurrentUserId(authentication);
        List<Registration> registrations = registrationRepository.findByAthleteIdOrderByCreatedTimeDesc(userId);
        model.addAttribute("registrations", registrations);
        return "registration/list";
    }

    /**
     * 报名表单页面
     */
    @GetMapping("/registrations/create")
    public String createPage(@RequestParam Long eventId, Model model) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));
        
        List<SportType> sportTypes = sportTypeRepository.findByEnabledTrueOrderBySortOrderAsc();
        
        model.addAttribute("event", event);
        model.addAttribute("sportTypes", sportTypes);
        return "registration/form";
    }

    /**
     * 提交报名
     */
    @PostMapping("/registrations")
    public String submitRegistration(
            @RequestParam Long eventId,
            @RequestParam(required = false) Long sportTypeId,
            @RequestParam(required = false) String group,
            @RequestParam(required = false) String remark,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            Long userId = getCurrentUserId(authentication);
            registrationService.registerAsUser(eventId, userId, sportTypeId, group, remark);
            redirectAttributes.addFlashAttribute("success", "报名成功！请等待审核。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/registrations";
    }

    /**
     * 取消报名
     */
    @PostMapping("/registrations/{id}/cancel")
    public String cancelRegistration(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            registrationService.cancelRegistration(id);
            redirectAttributes.addFlashAttribute("success", "报名已取消");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/registrations";
    }

    /**
     * 管理员 - 查看赛事报名列表
     */
    @GetMapping("/admin/registrations")
    @PreAuthorize("hasRole('ADMIN') or hasRole('REFEREE')")
    public String adminRegistrationList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Long eventId,
            Model model) {
        
        Page<Registration> registrations;
        if (eventId != null) {
            registrations = registrationService.getRegistrationsByEvent(eventId, PageRequest.of(page, 20));
            model.addAttribute("eventId", eventId);
        } else {
            registrations = registrationService.getPendingRegistrations(PageRequest.of(page, 20));
        }
        
        model.addAttribute("registrations", registrations);
        return "admin/registration-list";
    }

    /**
     * 管理员 - 审核报名（通过）
     */
    @PostMapping("/admin/registrations/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('REFEREE')")
    public String approveRegistration(
            @PathVariable Long id,
            @RequestParam(required = false) String bibNumber,
            @RequestParam(required = false) Integer lane,
            @RequestParam(required = false) String group,
            @RequestParam(required = false) String comment,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            Long reviewerId = getCurrentUserId(authentication);
            registrationService.reviewRegistration(id, "APPROVED", comment, reviewerId);
            redirectAttributes.addFlashAttribute("success", "审核通过！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/registrations";
    }

    /**
     * 管理员 - 拒绝报名
     */
    @PostMapping("/admin/registrations/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public String rejectRegistration(
            @PathVariable Long id,
            @RequestParam String comment,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            Long reviewerId = getCurrentUserId(authentication);
            registrationService.reviewRegistration(id, "REJECTED", comment, reviewerId);
            redirectAttributes.addFlashAttribute("success", "已拒绝该报名");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/registrations";
    }

    /**
     * 获取当前登录用户的ID
     */
    private Long getCurrentUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return user.getId();
    }
}
