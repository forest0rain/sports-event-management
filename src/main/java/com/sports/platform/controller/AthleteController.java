package com.sports.platform.controller;

import com.sports.platform.entity.Athlete;
import com.sports.platform.service.AthleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 运动员管理控制器
 */
@Controller
@RequestMapping("/athletes")
@RequiredArgsConstructor
public class AthleteController {

    private final AthleteService athleteService;

    /**
     * 运动员列表
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "10") int size,
                      @RequestParam(required = false) String keyword,
                      @RequestParam(required = false) String gender,
                      @RequestParam(required = false) String ageGroup,
                      Model model) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdTime").descending());
        
        Page<Athlete> athletes;
        if (keyword != null && !keyword.isEmpty()) {
            athletes = athleteService.searchAthletes(keyword, pageRequest);
        } else {
            athletes = athleteService.getAllAthletes(pageRequest);
        }

        model.addAttribute("athletes", athletes);
        model.addAttribute("keyword", keyword);
        model.addAttribute("gender", gender);
        model.addAttribute("ageGroup", ageGroup);
        model.addAttribute("genders", List.of("M", "F"));
        model.addAttribute("ageGroups", List.of("U18", "U20", "U23", "OPEN"));
        
        return "athlete/list";
    }

    /**
     * 运动员详情
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Athlete athlete = athleteService.getAthleteById(id);
        
        // 获取标签信息
        model.addAttribute("specialties", athleteService.getSpecialties(athlete));
        model.addAttribute("technicalFeatures", athleteService.getTechnicalFeatures(athlete));
        model.addAttribute("customTags", athleteService.getCustomTags(athlete));
        model.addAttribute("athlete", athlete);
        
        return "athlete/detail";
    }

    /**
     * 创建运动员页面
     */
    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("athlete", new Athlete());
        return "athlete/form";
    }

    /**
     * 创建运动员
     */
    @PostMapping("/create")
    public String create(@ModelAttribute Athlete athlete,
                        @RequestParam(required = false) Long userId,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "athlete/form";
        }

        try {
            athleteService.createAthlete(athlete, userId);
            redirectAttributes.addFlashAttribute("success", "运动员档案创建成功");
            return "redirect:/athletes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/athletes/create";
        }
    }

    /**
     * 编辑运动员页面
     */
    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, Model model) {
        Athlete athlete = athleteService.getAthleteById(id);
        model.addAttribute("athlete", athlete);
        return "athlete/form";
    }

    /**
     * 更新运动员
     */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                        @ModelAttribute Athlete athlete,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "athlete/form";
        }

        try {
            athleteService.updateAthlete(id, athlete);
            redirectAttributes.addFlashAttribute("success", "运动员信息更新成功");
            return "redirect:/athletes/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/athletes/" + id + "/edit";
        }
    }

    /**
     * 更新特长
     */
    @PostMapping("/{id}/specialties")
    public String updateSpecialties(@PathVariable Long id,
                                   @RequestParam List<String> specialties,
                                   RedirectAttributes redirectAttributes) {
        try {
            athleteService.updateSpecialties(id, specialties);
            redirectAttributes.addFlashAttribute("success", "特长更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/athletes/" + id;
    }

    /**
     * 更新自定义标签
     */
    @PostMapping("/{id}/tags")
    public String updateTags(@PathVariable Long id,
                            @RequestParam List<String> tags,
                            RedirectAttributes redirectAttributes) {
        try {
            athleteService.updateCustomTags(id, tags);
            redirectAttributes.addFlashAttribute("success", "标签更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/athletes/" + id;
    }

    /**
     * 删除运动员
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            athleteService.deleteAthlete(id);
            redirectAttributes.addFlashAttribute("success", "运动员已删除");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/athletes";
    }
}
