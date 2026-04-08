package com.sports.platform.controller;

import com.sports.platform.entity.User;
import com.sports.platform.repository.UserRepository;
import com.sports.platform.service.RoleApplicationService;
import com.sports.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 认证控制器
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RoleApplicationService applicationService;
    private final UserRepository userRepository;

    /**
     * 首页
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                           @RequestParam(required = false) String logout,
                           Model model) {
        if (error != null) {
            model.addAttribute("error", "用户名或密码错误");
        }
        if (logout != null) {
            model.addAttribute("message", "已成功退出登录");
        }
        return "auth/login";
    }

    /**
     * 注册页面
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    /**
     * 注册处理
     */
    @PostMapping("/register")
    public String register(@ModelAttribute User user,
                          @RequestParam String confirmPassword,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        // 验证密码
        if (!user.getPassword().equals(confirmPassword)) {
            result.rejectValue("password", "error.user", "两次密码不一致");
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.register(user.getUsername(), user.getPassword(), 
                    user.getEmail(), user.getRealName());
            model.addAttribute("registerSuccess", true);
            model.addAttribute("successMessage", "恭喜您，账号注册成功！");
            model.addAttribute("user", new User()); // 清空表单
            return "auth/register";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    /**
     * 仪表盘
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 添加待审核角色申请数量
        long pendingCount = applicationService.getPendingCount();
        model.addAttribute("pendingCount", pendingCount);
        return "dashboard/index";
    }

    /**
     * 个人信息页面
     */
    @GetMapping("/profile")
    public String profilePage(Model model) {
        return "redirect:/profile/info";
    }

    /**
     * 个人信息页面
     */
    @GetMapping("/profile/info")
    public String profileInfoPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        model.addAttribute("user", user);
        return "profile/info";
    }

    /**
     * 修改个人信息
     */
    @PostMapping("/profile/info")
    public String updateProfile(@ModelAttribute User user,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            currentUser.setRealName(user.getRealName());
            currentUser.setEmail(user.getEmail());
            currentUser.setPhone(user.getPhone());
            userRepository.save(currentUser);
            redirectAttributes.addFlashAttribute("success", "个人信息更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile/info";
    }

    /**
     * 修改密码
     */
    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "两次密码不一致");
            return "redirect:/profile/info";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "密码长度不能少于6位");
            return "redirect:/profile/info";
        }

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            userService.changePassword(user.getId(), oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "密码修改成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile/info";
    }
}
