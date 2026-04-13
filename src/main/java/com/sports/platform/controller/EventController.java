package com.sports.platform.controller;

import com.sports.platform.entity.Event;
import com.sports.platform.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * 赛事管理控制器
 */
@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * 赛事列表
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "10") int size,
                      @RequestParam(required = false) String keyword,
                      @RequestParam(required = false) String status,
                      Model model) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdTime").descending());
        
        Page<Event> events;
        if (keyword != null && !keyword.isEmpty()) {
            events = eventService.searchEvents(keyword, pageRequest);
        } else if (status != null && !status.isEmpty()) {
            events = eventService.getEventsByStatus(status, pageRequest);
        } else {
            events = eventService.getAllEvents(pageRequest);
        }

        model.addAttribute("events", events);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("statusList", List.of("DRAFT", "REGISTRATION", "ONGOING", "FINISHED", "CANCELLED"));
        
        return "event/list";
    }

    /**
     * 公开赛事列表(无需登录)
     */
    @GetMapping("/public")
    public String publicList(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("startDate").ascending());
        Page<Event> events = eventService.getPublicEvents(pageRequest);
        
        model.addAttribute("events", events);
        return "event/public";
    }

    /**
     * 赛事详情
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Event event = eventService.getEventById(id);
        model.addAttribute("event", event);
        return "event/detail";
    }

    /**
     * 创建赛事页面
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("event", new Event());
        model.addAttribute("eventTypes", List.of("校园", "企业", "社区", "市级", "省级"));
        return "event/form";
    }

    /**
     * 创建赛事
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    @PostMapping("/create")
    public String create(@ModelAttribute Event event,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "event/form";
        }

        try {
            eventService.createEvent(event);
            redirectAttributes.addFlashAttribute("success", "赛事创建成功");
            return "redirect:/events";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/events/create";
        }
    }

    /**
     * 编辑赛事页面
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, Model model) {
        Event event = eventService.getEventById(id);
        model.addAttribute("event", event);
        model.addAttribute("eventTypes", List.of("校园", "企业", "社区", "市级", "省级"));
        return "event/form";
    }

    /**
     * 更新赛事
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                        @ModelAttribute Event event,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "event/form";
        }

        try {
            eventService.updateEvent(id, event);
            redirectAttributes.addFlashAttribute("success", "赛事更新成功");
            return "redirect:/events/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/events/" + id + "/edit";
        }
    }

    /**
     * 发布赛事
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    @PostMapping("/{id}/publish")
    public String publish(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            eventService.publishEvent(id);
            redirectAttributes.addFlashAttribute("success", "赛事发布成功，已开放报名");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/events/" + id;
    }

    /**
     * 开始赛事
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    @PostMapping("/{id}/start")
    public String start(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            eventService.startEvent(id);
            redirectAttributes.addFlashAttribute("success", "赛事已开始");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/events/" + id;
    }

    /**
     * 结束赛事
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    @PostMapping("/{id}/finish")
    public String finish(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            eventService.finishEvent(id);
            redirectAttributes.addFlashAttribute("success", "赛事已结束");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/events/" + id;
    }

    /**
     * 取消赛事
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            eventService.cancelEvent(id);
            redirectAttributes.addFlashAttribute("success", "赛事已取消");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/events/" + id;
    }
    
    /**
     * 手动更新赛事状态
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, 
                              @RequestParam String status,
                              RedirectAttributes redirectAttributes) {
        try {
            eventService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "状态已更新");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/events/" + id;
    }

    /**
     * 删除赛事
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'REFEREE')")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            eventService.deleteEvent(id);
            redirectAttributes.addFlashAttribute("success", "赛事已删除");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/events";
    }
}
