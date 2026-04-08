package com.sports.platform.service;

import com.sports.platform.entity.Event;
import com.sports.platform.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 赛事服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    /**
     * 创建赛事
     */
    @Transactional
    public Event createEvent(Event event) {
        // 验证日期
        if (event.getEndDate().isBefore(event.getStartDate())) {
            throw new RuntimeException("结束日期不能早于开始日期");
        }
        
        if (event.getRegistrationDeadline() != null && 
            event.getRegistrationDeadline().isAfter(event.getStartDate())) {
            throw new RuntimeException("报名截止日期不能晚于赛事开始日期");
        }

        event.setStatus("DRAFT");
        event.setCurrentParticipants(0);
        
        event = eventRepository.save(event);
        log.info("创建赛事成功: {}", event.getName());
        
        return event;
    }

    /**
     * 更新赛事
     */
    @Transactional
    public Event updateEvent(Long id, Event eventDetails) {
        Event event = getEventById(id);
        
        event.setName(eventDetails.getName());
        event.setShortName(eventDetails.getShortName());
        event.setDescription(eventDetails.getDescription());
        event.setCoverImage(eventDetails.getCoverImage());
        event.setEventType(eventDetails.getEventType());
        event.setStartDate(eventDetails.getStartDate());
        event.setEndDate(eventDetails.getEndDate());
        event.setRegistrationDeadline(eventDetails.getRegistrationDeadline());
        event.setOrganizer(eventDetails.getOrganizer());
        event.setSponsor(eventDetails.getSponsor());
        event.setMaxParticipants(eventDetails.getMaxParticipants());
        event.setRules(eventDetails.getRules());
        event.setAwards(eventDetails.getAwards());
        event.setIsPublic(eventDetails.getIsPublic());
        
        return eventRepository.save(event);
    }

    /**
     * 发布赛事(状态变为报名中)
     */
    @Transactional
    public Event publishEvent(Long id) {
        Event event = getEventById(id);
        
        if (!"DRAFT".equals(event.getStatus())) {
            throw new RuntimeException("只有草稿状态的赛事可以发布");
        }
        
        event.setStatus("REGISTRATION");
        event = eventRepository.save(event);
        
        log.info("发布赛事: {}", event.getName());
        return event;
    }

    /**
     * 开始赛事(状态变为进行中)
     */
    @Transactional
    public Event startEvent(Long id) {
        Event event = getEventById(id);
        
        if (!"REGISTRATION".equals(event.getStatus())) {
            throw new RuntimeException("只有报名中的赛事可以开始");
        }
        
        event.setStatus("ONGOING");
        // 自动同步开始日期为当前日期
        if (event.getStartDate() == null || event.getStartDate().isAfter(LocalDate.now())) {
            event.setStartDate(LocalDate.now());
        }
        event = eventRepository.save(event);
        
        log.info("赛事开始: {}", event.getName());
        return event;
    }

    /**
     * 结束赛事
     */
    @Transactional
    public Event finishEvent(Long id) {
        Event event = getEventById(id);
        
        if (!"ONGOING".equals(event.getStatus())) {
            throw new RuntimeException("只有进行中的赛事可以结束");
        }
        
        event.setStatus("FINISHED");
        // 自动同步结束日期为当前日期
        event.setEndDate(LocalDate.now());
        event = eventRepository.save(event);
        
        log.info("赛事结束: {}", event.getName());
        return event;
    }
    
    /**
     * 手动更新赛事状态（管理员可强制修改）
     */
    @Transactional
    public Event updateStatus(Long id, String newStatus) {
        Event event = getEventById(id);
        String oldStatus = event.getStatus();
        
        event.setStatus(newStatus);
        
        // 根据状态自动同步日期
        LocalDate today = LocalDate.now();
        if ("ONGOING".equals(newStatus) && !event.getStartDate().isBefore(today)) {
            // 开始状态，同步开始日期
            event.setStartDate(today);
        } else if ("FINISHED".equals(newStatus)) {
            // 结束状态，同步结束日期
            event.setEndDate(today);
        }
        
        event = eventRepository.save(event);
        log.info("赛事状态变更: {} -> {} ({})", oldStatus, newStatus, event.getName());
        return event;
    }

    /**
     * 取消赛事
     */
    @Transactional
    public Event cancelEvent(Long id) {
        Event event = getEventById(id);
        
        if ("FINISHED".equals(event.getStatus())) {
            throw new RuntimeException("已结束的赛事不能取消");
        }
        
        event.setStatus("CANCELLED");
        event = eventRepository.save(event);
        
        log.info("取消赛事: {}", event.getName());
        return event;
    }

    /**
     * 删除赛事
     */
    @Transactional
    public void deleteEvent(Long id) {
        Event event = getEventById(id);
        
        if ("ONGOING".equals(event.getStatus())) {
            throw new RuntimeException("进行中的赛事不能删除");
        }
        
        eventRepository.delete(event);
        log.info("删除赛事: {}", event.getName());
    }

    /**
     * 获取赛事详情
     */
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));
    }

    /**
     * 获取所有赛事
     */
    public Page<Event> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    /**
     * 获取公开赛事
     */
    public Page<Event> getPublicEvents(Pageable pageable) {
        return eventRepository.findByIsPublicTrue(pageable);
    }

    /**
     * 根据状态获取赛事
     */
    public Page<Event> getEventsByStatus(String status, Pageable pageable) {
        return eventRepository.findByStatus(status, pageable);
    }

    /**
     * 获取活跃赛事(正在报名或进行中)
     */
    public List<Event> getActiveEvents() {
        return eventRepository.findActiveEvents();
    }

    /**
     * 搜索赛事
     */
    public Page<Event> searchEvents(String keyword, Pageable pageable) {
        return eventRepository.search(keyword, pageable);
    }

    /**
     * 更新参赛人数
     */
    @Transactional
    public void updateParticipantCount(Long eventId, int delta) {
        Event event = getEventById(eventId);
        event.setCurrentParticipants(event.getCurrentParticipants() + delta);
        eventRepository.save(event);
    }

    /**
     * 获取赛事统计数据
     */
    public List<Object[]> getEventStatistics() {
        return eventRepository.countByStatus();
    }
}
