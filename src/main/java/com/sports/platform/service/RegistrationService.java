package com.sports.platform.service;

import com.sports.platform.entity.*;
import com.sports.platform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 报名服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final SportTypeRepository sportTypeRepository;
    private final UserRepository userRepository;

    /**
     * 运动员报名参赛
     */
    @Transactional
    public Registration register(Long eventId, Long athleteId, Long sportTypeId, String group, String seedScore, String remark) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));
        
        SportType sportType = sportTypeRepository.findById(sportTypeId)
                .orElseThrow(() -> new RuntimeException("运动项目不存在"));

        checkRegistrationScope(event, "ATHLETE");

        if (!"REGISTRATION".equals(event.getStatus())) {
            throw new RuntimeException("赛事不在报名阶段");
        }

        if (event.getRegistrationDeadline() != null && 
            LocalDateTime.now().isAfter(event.getRegistrationDeadline().atTime(23, 59, 59))) {
            throw new RuntimeException("已过报名截止日期");
        }

        if (registrationRepository.existsByEventIdAndAthleteId(eventId, athleteId)) {
            throw new RuntimeException("该运动员已报名此赛事");
        }

        if (event.getMaxParticipants() != null && 
            event.getCurrentParticipants() >= event.getMaxParticipants()) {
            throw new RuntimeException("报名人数已满");
        }

        Registration registration = Registration.builder()
                .eventId(eventId)
                .athleteId(athleteId)
                .sportTypeId(sportTypeId)
                .status(event.getRequireApproval() ? "PENDING" : "APPROVED")
                .group(group)
                .seedScore(seedScore)
                .build();

        registration = registrationRepository.save(registration);

        if (!event.getRequireApproval()) {
            String bibNumber = generateBibNumber(registration);
            registration.setBibNumber(bibNumber);
            event.setCurrentParticipants(event.getCurrentParticipants() + 1);
            eventRepository.save(event);
            registration = registrationRepository.save(registration);
        }
        
        log.info("运动员报名成功: {} - {} - {}", athleteId, event.getName(), sportType.getName());
        
        return registration;
    }

    /**
     * 普通用户报名参赛
     */
    @Transactional
    public Registration registerAsUser(Long eventId, Long userId, Long sportTypeId, String group, String remark) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));
        
        if (!"REGISTRATION".equals(event.getStatus())) {
            throw new RuntimeException("赛事不在报名阶段");
        }

        if (event.getRegistrationDeadline() != null && 
            LocalDateTime.now().isAfter(event.getRegistrationDeadline().atTime(23, 59, 59))) {
            throw new RuntimeException("已过报名截止日期");
        }

        List<Registration> existingRegs = registrationRepository.findByUserId(userId);
        boolean alreadyRegistered = existingRegs.stream()
                .anyMatch(r -> r.getEventId().equals(eventId) && !"CANCELLED".equals(r.getStatus()));
        if (alreadyRegistered) {
            throw new RuntimeException("您已报名此赛事");
        }

        if (event.getMaxParticipants() != null && 
            event.getCurrentParticipants() >= event.getMaxParticipants()) {
            throw new RuntimeException("报名人数已满");
        }

        Registration registration = Registration.builder()
                .eventId(eventId)
                .athleteId(userId)
                .sportTypeId(sportTypeId)
                .status(event.getRequireApproval() ? "PENDING" : "APPROVED")
                .group(group)
                .remark(remark)
                .build();

        registration = registrationRepository.save(registration);

        if (!event.getRequireApproval()) {
            String bibNumber = generateBibNumberForUser(registration);
            registration.setBibNumber(bibNumber);
            event.setCurrentParticipants(event.getCurrentParticipants() + 1);
            eventRepository.save(event);
            registration = registrationRepository.save(registration);
        }
        
        log.info("普通用户报名成功: {} - {}", userId, event.getName());
        
        return registration;
    }

    /**
     * 检查报名资格
     */
    private void checkRegistrationScope(Event event, String userType) {
        String scope = event.getRegistrationScope();
        if (scope == null) {
            scope = "ALL";
        }
        
        switch (scope) {
            case "ALL":
                break;
            case "ATHLETE":
                if (!"ATHLETE".equals(userType)) {
                    throw new RuntimeException("此赛事仅允许运动员报名");
                }
                break;
            case "STUDENT":
                break;
            case "STAFF":
                break;
            default:
                break;
        }
    }

    /**
     * 审核报名
     */
    @Transactional
    public Registration reviewRegistration(Long id, String status, String comment, Long reviewerId) {
        Registration registration = getRegistrationById(id);
        
        if (!"PENDING".equals(registration.getStatus())) {
            throw new RuntimeException("该报名已审核");
        }

        registration.setStatus(status);
        registration.setReviewComment(comment);
        registration.setReviewerId(reviewerId);
        registration.setReviewTime(LocalDateTime.now());

        if ("APPROVED".equals(status)) {
            String bibNumber = generateBibNumber(registration);
            registration.setBibNumber(bibNumber);
            
            Event event = registrationRepository.findById(id).get().getEventId() != null 
                    ? eventRepository.findById(registrationRepository.findById(id).get().getEventId()).orElse(null) : null;
            if (event != null) {
                event.setCurrentParticipants(event.getCurrentParticipants() + 1);
                eventRepository.save(event);
            }
        }

        registration = registrationRepository.save(registration);
        
        log.info("报名审核完成: {} - {}", id, status);
        
        return registration;
    }

    /**
     * 生成参赛号码
     */
    private String generateBibNumber(Registration registration) {
        String sportCode = String.format("%03d", registration.getSportTypeId());
        String athleteCode = String.format("%04d", registration.getAthleteId());
        String randomCode = UUID.randomUUID().toString().substring(0, 3).toUpperCase();
        
        return sportCode + athleteCode + randomCode;
    }

    /**
     * 为普通用户生成参赛号码
     */
    private String generateBibNumberForUser(Registration registration) {
        String sportCode = String.format("%03d", registration.getSportTypeId());
        String userCode = "U" + String.format("%04d", registration.getAthleteId());
        String randomCode = UUID.randomUUID().toString().substring(0, 3).toUpperCase();
        
        return sportCode + userCode + randomCode;
    }

    /**
     * 取消报名
     */
    @Transactional
    public void cancelRegistration(Long id) {
        Registration registration = getRegistrationById(id);
        
        if ("APPROVED".equals(registration.getStatus())) {
            Event event = eventRepository.findById(registration.getEventId()).orElse(null);
            if (event != null) {
                event.setCurrentParticipants(Math.max(0, event.getCurrentParticipants() - 1));
                eventRepository.save(event);
            }
        }

        registration.setStatus("CANCELLED");
        registrationRepository.save(registration);
        
        log.info("取消报名: {}", id);
    }

    /**
     * 获取报名详情
     */
    public Registration getRegistrationById(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("报名记录不存在"));
    }

    /**
     * 根据赛事获取报名列表
     */
    public Page<Registration> getRegistrationsByEvent(Long eventId, Pageable pageable) {
        return registrationRepository.findByEventId(eventId, pageable);
    }

    /**
     * 根据运动员获取报名列表
     */
    public Page<Registration> getRegistrationsByAthlete(Long athleteId, Pageable pageable) {
        return registrationRepository.findByAthleteId(athleteId, pageable);
    }

    /**
     * 根据用户获取报名列表
     */
    public List<Registration> getRegistrationsByUser(Long userId) {
        return registrationRepository.findByUserId(userId);
    }

    /**
     * 根据状态获取报名列表
     */
    public Page<Registration> getRegistrationsByStatus(String status, Pageable pageable) {
        return registrationRepository.findByStatus(status, pageable);
    }

    /**
     * 获取待审核的报名
     */
    public Page<Registration> getPendingRegistrations(Pageable pageable) {
        return registrationRepository.findByStatus("PENDING", pageable);
    }

    /**
     * 获取已审核通过的报名
     */
    public List<Registration> getApprovedRegistrations(Long eventId) {
        return registrationRepository.findApprovedByEventId(eventId);
    }

    /**
     * 获取赛事特定项目的报名
     */
    public List<Registration> getRegistrationsByEventAndSport(Long eventId, Long sportTypeId) {
        return registrationRepository.findByEventIdAndSportTypeId(eventId, sportTypeId);
    }

    /**
     * 统计赛事报名情况
     */
    public List<Object[]> getRegistrationStatistics(Long eventId) {
        return registrationRepository.countByEventIdAndStatus(eventId);
    }

    /**
     * 批量审核报名
     */
    @Transactional
    public void batchApprove(List<Long> ids, Long reviewerId) {
        for (Long id : ids) {
            reviewRegistration(id, "APPROVED", "批量审核通过", reviewerId);
        }
    }

    /**
     * 批量拒绝报名
     */
    @Transactional
    public void batchReject(List<Long> ids, String reason, Long reviewerId) {
        for (Long id : ids) {
            reviewRegistration(id, "REJECTED", reason, reviewerId);
        }
    }
}
