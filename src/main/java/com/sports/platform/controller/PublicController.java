package com.sports.platform.controller;

import com.sports.platform.entity.Event;
import com.sports.platform.entity.Result;
import com.sports.platform.entity.Schedule;
import com.sports.platform.repository.EventRepository;
import com.sports.platform.repository.ResultRepository;
import com.sports.platform.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 公开内容控制器
 * 观众无需注册即可浏览公开赛事信息、赛程安排和比赛成绩
 */
@Controller
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

    private final EventRepository eventRepository;
    private final ScheduleRepository scheduleRepository;
    private final ResultRepository resultRepository;

    @GetMapping("/events")
    public String publicEvents(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "12") int size,
                               Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        // 只展示已发布及以后状态的赛事
        Page<Event> events = eventRepository.findByStatusNot(Event.EventStatus.DRAFT, pageable);
        model.addAttribute("events", events);
        model.addAttribute("pageTitle", "赛事信息");
        return "public/events";
    }

    @GetMapping("/schedules")
    public String publicSchedules(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "12") int size,
                                  Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "scheduleDate", "startTime"));
        Page<Schedule> schedules = scheduleRepository.findAll(pageable);
        model.addAttribute("schedules", schedules);
        model.addAttribute("pageTitle", "赛程安排");
        return "public/schedules";
    }

    @GetMapping("/results")
    public String publicResults(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "12") int size,
                                Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        // 只展示已审核确认的成绩
        Page<Result> results = resultRepository.findByReviewStatus("CONFIRMED", pageable);
        model.addAttribute("results", results);
        model.addAttribute("pageTitle", "比赛成绩");
        return "public/results";
    }
}
