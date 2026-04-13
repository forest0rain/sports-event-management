package com.sports.platform.controller;

import com.sports.platform.entity.Event;
import com.sports.platform.entity.Registration;
import com.sports.platform.entity.SportType;
import com.sports.platform.entity.User;
import com.sports.platform.repository.EventRepository;
import com.sports.platform.repository.SportTypeRepository;
import com.sports.platform.repository.UserRepository;
import com.sports.platform.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 报名管理控制器
 */
@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final EventRepository eventRepository;
    private final SportTypeRepository sportTypeRepository;
    private final UserRepository userRepository;

    /**
     * 我的报名列表
     */
    @GetMapping("/registrations")
    public String myRegistrations(Authentication authentication, Model model) {
        Long userId = getCurrentUserId(authentication);
        List<Registration> registrations = registrationService.getRegistrationsByUser(userId);
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
        
        // 先获取该赛事的运动项目
        List<SportType> sportTypes = sportTypeRepository.findByEventId(eventId);
        
        // 如果赛事没有关联运动项目，获取所有启用的运动项目
        if (sportTypes == null || sportTypes.isEmpty()) {
            sportTypes = sportTypeRepository.findByEnabledTrueOrderBySortOrderAsc();
        }
        
        model.addAttribute("event", event);
        model.addAttribute("sportTypes", sportTypes);
        return "registration/form";
    }

    /**
     * 提交报名
     */
    @PostMapping("/registrations")
    public String register(@RequestParam Long eventId,
                          @RequestParam Long sportTypeId,
                          @RequestParam String registrantName,
                          @RequestParam String registrantPhone,
                          @RequestParam(required = false) String registrantOrg,
                          @RequestParam(required = false) String remark,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        try {
            Long userId = getCurrentUserId(authentication);
            registrationService.registerAsUser(eventId, userId, sportTypeId, 
                    registrantName, registrantPhone, registrantOrg, remark);
            redirectAttributes.addFlashAttribute("success", "报名成功！");
            return "redirect:/registrations";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registrations/create?eventId=" + eventId;
        }
    }

    /**
     * 取消报名
     */
    @PostMapping("/registrations/{id}/cancel")
    public String cancel(@PathVariable Long id,
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
     * 管理员/裁判 - 报名审核列表
     */
    @GetMapping("/admin/registrations")
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    public String adminList(@RequestParam(required = false) Long eventId,
                           @RequestParam(required = false) String status,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           Model model) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdTime").descending());
        
        Page<Registration> registrations;
        if (eventId != null && status != null && !status.isEmpty()) {
            // 根据赛事和状态筛选
            registrations = registrationService.getRegistrationsByEvent(eventId, pageRequest);
            registrations = registrations.map(r -> r);
        } else if (eventId != null) {
            registrations = registrationService.getRegistrationsByEvent(eventId, pageRequest);
        } else if (status != null && !status.isEmpty()) {
            registrations = registrationService.getRegistrationsByStatus(status, pageRequest);
        } else {
            registrations = registrationService.getPendingRegistrations(pageRequest);
        }
        
        List<Event> events = eventRepository.findAll();
        
        model.addAttribute("registrations", registrations);
        model.addAttribute("events", events);
        model.addAttribute("eventId", eventId);
        model.addAttribute("status", status);
        model.addAttribute("statusList", List.of("PENDING", "APPROVED", "REJECTED", "CANCELLED"));
        
        return "admin/registrations";
    }

    /**
     * 审核通过
     */
    @PostMapping("/admin/registrations/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    public String approve(@PathVariable Long id,
                        @RequestParam(required = false) String comment,
                        Authentication authentication,
                        RedirectAttributes redirectAttributes) {
        try {
            Long reviewerId = getCurrentUserId(authentication);
            registrationService.reviewRegistration(id, "APPROVED", comment, reviewerId);
            redirectAttributes.addFlashAttribute("success", "报名已通过");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/registrations";
    }

    /**
     * 审核拒绝
     */
    @PostMapping("/admin/registrations/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    public String reject(@PathVariable Long id,
                        @RequestParam String reason,
                        Authentication authentication,
                        RedirectAttributes redirectAttributes) {
        try {
            Long reviewerId = getCurrentUserId(authentication);
            registrationService.reviewRegistration(id, "REJECTED", reason, reviewerId);
            redirectAttributes.addFlashAttribute("success", "报名已拒绝");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/registrations";
    }

    /**
     * 批量审核通过
     */
    @PostMapping("/admin/registrations/batch-approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    public String batchApprove(@RequestParam List<Long> ids,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            Long reviewerId = getCurrentUserId(authentication);
            registrationService.batchApprove(ids, reviewerId);
            redirectAttributes.addFlashAttribute("success", "批量审核成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/registrations";
    }

    /**
     * 批量审核拒绝
     */
    @PostMapping("/admin/registrations/batch-reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    public String batchReject(@RequestParam List<Long> ids,
                             @RequestParam String reason,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            Long reviewerId = getCurrentUserId(authentication);
            registrationService.batchReject(ids, reason, reviewerId);
            redirectAttributes.addFlashAttribute("success", "批量拒绝成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/registrations";
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return user.getId();
    }
}
