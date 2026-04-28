package com.sports.platform.controller;

import com.sports.platform.entity.User;
import com.sports.platform.repository.RoleRepository;
import com.sports.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 用户管理控制器
 */
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    /**
     * 用户列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String role,
                       Model model) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdTime").descending());
        
        Page<User> users;
        if (keyword != null && !keyword.isEmpty()) {
            users = userService.searchUsers(keyword, pageRequest);
        } else if (role != null && !role.isEmpty()) {
            users = userService.getUsersByRole(role, pageRequest);
        } else {
            users = userService.getAllUsers(pageRequest);
        }

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("roles", roleRepository.findAll());
        
        return "admin/user-list";
    }

    /**
     * 用户详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String detail(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "admin/user-detail";
    }

    /**
     * 创建用户页面
     */
    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createPage(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/user-form";
    }

    /**
     * 创建用户
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@ModelAttribute User user,
                         @RequestParam List<Long> roleIds,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/user-form";
        }

        try {
            List<String> roleCodes = roleRepository.findAllById(roleIds)
                    .stream()
                    .map(r -> r.getCode())
                    .toList();
            userService.createUser(user, roleCodes);
            redirectAttributes.addFlashAttribute("success", "用户创建成功");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users/create";
        }
    }

    /**
     * 编辑用户页面
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editPage(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/user-form";
    }

    /**
     * 更新用户
     */
    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable Long id,
                         @ModelAttribute User user,
                         @RequestParam List<Long> roleIds,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/user-form";
        }

        try {
            userService.updateUser(id, user);
            if (!roleIds.isEmpty()) {
                List<String> roleCodes = roleRepository.findAllById(roleIds)
                        .stream()
                        .map(r -> r.getCode())
                        .toList();
                userService.updateUserRoles(id, roleCodes);
            }
            redirectAttributes.addFlashAttribute("success", "用户信息更新成功");
            return "redirect:/admin/users/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users/" + id + "/edit";
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public String resetPassword(@PathVariable Long id,
                                 @RequestParam String newPassword,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.resetPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("success", "密码重置成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    /**
     * 启用/禁用用户
     */
    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleStatus(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("success", "用户状态更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * 删除用户
     */
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "用户删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * 修改个人密码页面
     */
    @GetMapping("/change-password")
    public String changePasswordPage(Model model) {
        model.addAttribute("user", new User());
        return "profile/change-password";
    }

    /**
     * 修改个人密码
     */
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                  @RequestParam String newPassword,
                                  @RequestParam String confirmPassword,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        // 验证新密码确认
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "两次输入的新密码不一致");
            return "redirect:/admin/users/change-password";
        }

        // 验证新密码长度
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "新密码长度不能少于6位");
            return "redirect:/admin/users/change-password";
        }

        try {
            User currentUser = userService.getUserByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            userService.changePassword(currentUser.getId(), oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "密码修改成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users/change-password";
    }
}
