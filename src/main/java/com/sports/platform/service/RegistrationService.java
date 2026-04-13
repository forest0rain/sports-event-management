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
    private final AthleteRepository athleteRepository;
    private final SportTypeRepository sportTypeRepository;
    private final UserRepository userRepository;

    /**
     * 运动员报名参赛
     */
    @Transactional
    public Registration register(Long eventId, Long athleteId, Long sportTypeId, String seedScore, String remark) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));
        
        Athlete athlete = athleteRepository.findById(athleteId)
                .orElseThrow(() -> new RuntimeException("运动员不存在"));
        
        SportType sportType = sportTypeRepository.findById(sportTypeId)
                .orElseThrow(() -> new RuntimeException("运动项目不存在"));

        // 检查报名资格
        checkRegistrationScope(event, "ATHLETE");

        // 检查赛事状态
        if (!"REGISTRATION".equals(event.getStatus())) {
            throw new RuntimeException("赛事不在报名阶段");
        }

        // 检查报名截止日期
        if (event.getRegistrationDeadline() != null && 
            LocalDateTime.now().isAfter(event.getRegistrationDeadline().atTime(23, 59, 59))) {
            throw new RuntimeException("已过报名截止日期");
        }

        // 检查是否已报名
        if (registrationRepository.existsByEventIdAndAthleteId(eventId, athleteId)) {
            throw new RuntimeException("该运动员已报名此赛事");
        }

        // 检查人数限制
        if (event.getMaxParticipants() != null && 
            event.getCurrentParticipants() >= event.getMaxParticipants()) {
            throw new RuntimeException("报名人数已满");
        }

        // 确定组别
        String group = athlete.getGender().equals("M") ? "男子组" : "女子组";

        // 创建报名
        Registration registration = Registration.builder()
                .event(event)
                .athlete(athlete)
                .sportType(sportType)
                .status(event.getRequireApproval() ? "PENDING" : "APPROVED")
                .group(group)
                .seedScore(seedScore)
                .remark(remark)
                .registrantName(athlete.getName())
                .build();

        registration = registrationRepository.save(registration);

        // 如果不需要审核，直接生成参赛号码
        if (!event.getRequireApproval()) {
            String bibNumber = generateBibNumber(registration);
            registration.setBibNumber(bibNumber);
            event.setCurrentParticipants(event.getCurrentParticipants() + 1);
            eventRepository.save(event);
            registration = registrationRepository.save(registration);
        }
        
        log.info("运动员报名成功: {} - {} - {}", athlete.getName(), event.getName(), sportType.getName());
        
        return registration;
    }

    /**
     * 普通用户报名参赛
     */
    @Transactional
    public Registration registerAsUser(Long eventId, Long userId, Long sportTypeId,
                                        String registrantName, String registrantPhone, 
                                        String registrantOrg, String remark) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        SportType sportType = sportTypeRepository.findById(sportTypeId)
                .orElseThrow(() -> new RuntimeException("运动项目不存在"));

        // 检查报名资格
        checkRegistrationScope(event, "USER");

        // 检查赛事状态
        if (!"REGISTRATION".equals(event.getStatus())) {
            throw new RuntimeException("赛事不在报名阶段");
        }

        // 检查报名截止日期
        if (event.getRegistrationDeadline() != null && 
            LocalDateTime.now().isAfter(event.getRegistrationDeadline().atTime(23, 59, 59))) {
            throw new RuntimeException("已过报名截止日期");
        }

        // 检查是否已报名（根据userId检查）
        List<Registration> existingRegs = registrationRepository.findByUserId(userId);
        boolean alreadyRegistered = existingRegs.stream()
                .anyMatch(r -> r.getEvent().getId().equals(eventId) && !"CANCELLED".equals(r.getStatus()));
        if (alreadyRegistered) {
            throw new RuntimeException("您已报名此赛事");
        }

        // 检查人数限制
        if (event.getMaxParticipants() != null && 
            event.getCurrentParticipants() >= event.getMaxParticipants()) {
            throw new RuntimeException("报名人数已满");
        }

        // 创建报名
        Registration registration = Registration.builder()
                .event(event)
                .user(user)
                .sportType(sportType)
                .status(event.getRequireApproval() ? "PENDING" : "APPROVED")
                .registrantName(registrantName)
                .registrantPhone(registrantPhone)
                .registrantOrg(registrantOrg)
                .remark(remark)
                .build();

        registration = registrationRepository.save(registration);

        // 如果不需要审核，直接生成参赛号码
        if (!event.getRequireApproval()) {
            String bibNumber = generateBibNumberForUser(registration);
            registration.setBibNumber(bibNumber);
            event.setCurrentParticipants(event.getCurrentParticipants() + 1);
            eventRepository.save(event);
            registration = registrationRepository.save(registration);
        }
        
        log.info("普通用户报名成功: {} - {} - {}", registrantName, event.getName(), sportType.getName());
        
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
                // 所有人都可以报名
                break;
            case "ATHLETE":
                // 只有运动员可以报名
                if (!"ATHLETE".equals(userType)) {
                    throw new RuntimeException("此赛事仅允许运动员报名");
                }
                break;
            case "STUDENT":
                // 学生可以报名（需要扩展角色检查）
                break;
            case "STAFF":
                // 教职工可以报名（需要扩展角色检查）
                break;
            default:
                break;
        }
    }

    /**
     * 审核报名(管理员/裁判)
     */
    @Transactional
    public Registration reviewRegistration(Long id, String status, String comment, Long reviewerId) {
        Registration registration = getRegistrationById(id);
        
        if (!"PENDING".equals(registration.getStatus())) {
            throw new RuntimeException("该报名已审核");
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("审核人不存在"));

        registration.setStatus(status);
        registration.setReviewComment(comment);
        registration.setReviewer(reviewer);
        registration.setReviewTime(LocalDateTime.now());

        // 如果审核通过，生成参赛号码
        if ("APPROVED".equals(status)) {
            String bibNumber = registration.getAthlete() != null 
                    ? generateBibNumber(registration) 
                    : generateBibNumberForUser(registration);
            registration.setBibNumber(bibNumber);
            
            // 更新赛事参赛人数
            Event event = registration.getEvent();
            event.setCurrentParticipants(event.getCurrentParticipants() + 1);
            eventRepository.save(event);
        }

        registration = registrationRepository.save(registration);
        
        String registrantName = registration.getRegistrantName();
        log.info("报名审核完成: {} - {}", registrantName, status);
        
        return registration;
    }

    /**
     * 生成参赛号码
     */
    private String generateBibNumber(Registration registration) {
        // 格式: 项目代码 + 运动员ID + 随机数
        String sportCode = registration.getSportType().getCode();
        if (sportCode == null) {
            sportCode = String.format("%03d", registration.getSportType().getId());
        }
        
        String athleteCode = String.format("%04d", registration.getAthlete().getId());
        String randomCode = UUID.randomUUID().toString().substring(0, 3).toUpperCase();
        
        return sportCode + athleteCode + randomCode;
    }

    /**
     * 为普通用户生成参赛号码
     */
    private String generateBibNumberForUser(Registration registration) {
        // 格式: 项目代码 + U + 用户ID + 随机数
        String sportCode = registration.getSportType().getCode();
        if (sportCode == null) {
            sportCode = String.format("%03d", registration.getSportType().getId());
        }
        
        String userCode = "U" + String.format("%04d", registration.getUser().getId());
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
            // 减少赛事参赛人数
            Event event = registration.getEvent();
            event.setCurrentParticipants(Math.max(0, event.getCurrentParticipants() - 1));
            eventRepository.save(event);
        }

        registration.setStatus("CANCELLED");
        registrationRepository.save(registration);
        
        log.info("取消报名: {}", registration.getRegistrantName());
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
