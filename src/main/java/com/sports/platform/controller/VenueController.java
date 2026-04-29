package com.sports.platform.controller;

import com.sports.platform.entity.Venue;
import com.sports.platform.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 场地管理控制器
 */
@Controller
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    /**
     * 场地列表
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "10") int size,
                      @RequestParam(required = false) String keyword,
                      @RequestParam(required = false) String type,
                      Model model) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdTime").descending());

        Page<Venue> venues;
        if (keyword != null && !keyword.isEmpty()) {
            venues = venueService.searchVenues(keyword, pageRequest);
        } else {
            venues = venueService.getAllVenues(pageRequest);
        }

        model.addAttribute("venues", venues);
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);
        model.addAttribute("venueTypes", List.of("TRACK", "GYMNASIUM", "SWIMMING", "FIELD", "MULTIPURPOSE"));
        return "venue/list";
    }

    /**
     * 场地详情
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Venue venue = venueService.getVenueById(id);
        model.addAttribute("venue", venue);
        return "venue/detail";
    }

    /**
     * 创建场地页面
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("venue", new Venue());
        model.addAttribute("venueTypes", List.of("TRACK", "GYMNASIUM", "SWIMMING", "FIELD", "MULTIPURPOSE"));
        return "venue/form";
    }

    /**
     * 创建场地
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String create(Venue venue, RedirectAttributes redirectAttributes) {
        try {
            venueService.createVenue(venue);
            redirectAttributes.addFlashAttribute("success", "场地创建成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "创建失败: " + e.getMessage());
        }
        return "redirect:/venues";
    }

    /**
     * 编辑场地页面
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, Model model) {
        Venue venue = venueService.getVenueById(id);
        model.addAttribute("venue", venue);
        model.addAttribute("venueTypes", List.of("TRACK", "GYMNASIUM", "SWIMMING", "FIELD", "MULTIPURPOSE"));
        return "venue/form";
    }

    /**
     * 更新场地
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}")
    public String update(@PathVariable Long id, Venue venue, RedirectAttributes redirectAttributes) {
        try {
            venueService.updateVenue(id, venue);
            redirectAttributes.addFlashAttribute("success", "场地更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "更新失败: " + e.getMessage());
        }
        return "redirect:/venues";
    }

    /**
     * 启用/禁用场地
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/toggle")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            venueService.toggleVenueStatus(id);
            redirectAttributes.addFlashAttribute("success", "状态已更新");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        return "redirect:/venues";
    }

    /**
     * 删除场地
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            venueService.deleteVenue(id);
            redirectAttributes.addFlashAttribute("success", "场地已删除");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/venues";
    }
}
