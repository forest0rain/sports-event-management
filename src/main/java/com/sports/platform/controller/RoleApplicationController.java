package com.sports.platform.controller;

import com.sports.platform.entity.RoleApplication;
import com.sports.platform.entity.User;
import com.sports.platform.repository.UserRepository;
import com.sports.platform.service.FileUploadService;
import com.sports.platform.service.RoleApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色申请控制器
 */
@Controller
@RequiredArgsConstructor
public class RoleApplicationController {

    private final RoleApplicationService applicationService;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    /**
     * 角色申请页面
     */
    @GetMapping("/profile/role-application")
    public String applicationPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查是否已有待审核的申请
        boolean hasPending = applicationService.hasPendingApplication(user);
        model.addAttribute("hasPending", hasPending);

        // 获取申请历史
        List<RoleApplication> applications = applicationService.getUserApplications(user);
        model.addAttribute("applications", applications);

        // 检查用户当前角色
        boolean isAthlete = user.getRoles().stream()
                .anyMatch(role -> role.getCode().equals("ROLE_ATHLETE"));
        boolean isReferee = user.getRoles().stream()
                .anyMatch(role -> role.getCode().equals("ROLE_REFEREE"));
        model.addAttribute("isAthlete", isAthlete);
        model.addAttribute("isReferee", isReferee);

        return "profile/role-application";
    }

    /**
     * 提交角色申请
     */
    @PostMapping("/profile/role-application")
    public String submitApplication(@AuthenticationPrincipal UserDetails userDetails,
                                    @RequestParam String requestedRole,
                                    @RequestParam(required = false) String reason,
                                    @RequestParam(required = false) String qualification,
                                    @RequestParam(required = false) String contactPhone,
                                    @RequestParam(required = false) MultipartFile qualificationFile,
                                    RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 处理文件上传
            String filePath = null;
            String fileName = null;
            if (qualificationFile != null && !qualificationFile.isEmpty()) {
                filePath = fileUploadService.uploadQualificationFile(qualificationFile);
                fileName = qualificationFile.getOriginalFilename();
            }

            applicationService.submitApplication(user, requestedRole, reason, qualification,
                    contactPhone, filePath, fileName);
            redirectAttributes.addFlashAttribute("success", "申请已提交，请等待管理员审核");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile/role-application";
    }

    /**
     * 管理员 - 申请审核列表
     */
    @GetMapping("/admin/role-applications")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminApplicationList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "PENDING") String status,
            Model model) {

        Page<RoleApplication> applications;
        if ("PENDING".equals(status)) {
            applications = applicationService.getPendingApplications(
                    PageRequest.of(page, 10, Sort.by("createdTime").descending()));
        } else {
            applications = applicationService.getAllApplications(
                    PageRequest.of(page, 10, Sort.by("createdTime").descending()));
        }

        model.addAttribute("applications", applications);
        model.addAttribute("status", status);
        model.addAttribute("pendingCount", applicationService.getPendingCount());

        return "admin/role-applications";
    }

    /**
     * 管理员 - 查看申请详情
     */
    @GetMapping("/admin/role-applications/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String applicationDetail(@PathVariable Long id, Model model) {
        RoleApplication application = applicationService.getApplicationById(id);

        Map<String, Object> appData = new HashMap<>();
        appData.put("id", application.getId());
        appData.put("requestedRole", application.getRequestedRole());
        appData.put("reason", application.getReason());
        appData.put("qualification", application.getQualification());
        appData.put("qualificationFile", application.getQualificationFile());
        appData.put("qualificationFileName", application.getQualificationFileName());
        appData.put("contactPhone", application.getContactPhone());
        appData.put("status", application.getStatus());
        appData.put("reviewComment", application.getReviewComment());
        appData.put("reviewTime", application.getReviewTime());
        appData.put("createdTime", application.getCreatedTime());

        // 用户信息
        if (application.getUser() != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", application.getUser().getId());
            userData.put("username", application.getUser().getUsername());
            userData.put("realName", application.getUser().getRealName());
            userData.put("email", application.getUser().getEmail());

            List<String> roleNames = application.getUser().getRoles().stream()
                    .map(role -> role.getName())
                    .toList();

            userData.put("roleNames", roleNames);
            appData.put("user", userData);
        }

        // 审核人
        if (application.getReviewer() != null) {
            Map<String, Object> reviewerData = new HashMap<>();
            reviewerData.put("id", application.getReviewer().getId());
            reviewerData.put("realName", application.getReviewer().getRealName());
            reviewerData.put("username", application.getReviewer().getUsername());
            appData.put("reviewer", reviewerData);
        }

        // ⭐⭐⭐关键改动：变量名改掉
        model.addAttribute("roleApplication", appData);

        return "admin/role-application-detail";
    }

    /**
     * 管理员 - 审核通过
     */
    @PostMapping("/admin/role-applications/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public String approveApplication(@PathVariable Long id,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     @RequestParam(required = false) String comment,
                                     RedirectAttributes redirectAttributes) {
        try {
            User reviewer = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("审核人不存在"));

            applicationService.approveApplication(id, reviewer.getId(), comment);
            redirectAttributes.addFlashAttribute("success", "申请已通过");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/role-applications";
    }

    /**
     * 管理员 - 审核拒绝
     */
    @PostMapping("/admin/role-applications/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public String rejectApplication(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    @RequestParam(required = false) String comment,
                                    RedirectAttributes redirectAttributes) {
        try {
            User reviewer = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("审核人不存在"));

            applicationService.rejectApplication(id, reviewer.getId(), comment);
            redirectAttributes.addFlashAttribute("success", "申请已拒绝");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/role-applications";
    }
}
