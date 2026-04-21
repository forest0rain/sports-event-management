package com.sports.platform.service;

import com.sports.platform.entity.*;
import com.sports.platform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 赛程服务层
 * 
 * 核心功能:
 * 1. 智能赛程编排算法(分阶段贪婪策略)
 * 2. 赛程自动生成
 * 3. 人工调整
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final EventRepository eventRepository;
    private final SportTypeRepository sportTypeRepository;
    private final VenueRepository venueRepository;
    private final RegistrationRepository registrationRepository;
    private final AthleteRepository athleteRepository;

    /**
     * 智能赛程编排算法 - 分阶段贪婪策略
     * 
     * 算法思想:
     * 1. 第一阶段: 按项目和性别分组
     * 2. 第二阶段: 根据场地和时间贪婪填充
     * 3. 优先安排决赛，再安排预赛
     * 
     * @param eventId 赛事ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param dailyStartTime 每天开始时间
     * @param dailyEndTime 每天结束时间
     */
    @Transactional
    public List<Schedule> generateScheduleSmart(Long eventId, LocalDate startDate, LocalDate endDate,
                                                 LocalTime dailyStartTime, LocalTime dailyEndTime) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));

        // 获取已审核通过的报名
        List<Registration> registrations = registrationRepository.findByEventIdAndStatus(eventId, "APPROVED");
        if (registrations.isEmpty()) {
            throw new RuntimeException("暂无已审核的报名，无法生成赛程");
        }

        // 获取可用场地
        List<Venue> venues = venueRepository.findByEnabledTrue();
        if (venues.isEmpty()) {
            throw new RuntimeException("暂无可用场地");
        }

        // 删除已有赛程
        List<Schedule> existingSchedules = scheduleRepository.findByEventId(eventId);
        scheduleRepository.deleteAll(existingSchedules);

        // ========== 第一阶段: 分组 ==========
        // 按运动项目分组
        Map<Long, List<Registration>> registrationsBySport = registrations.stream()
                .collect(Collectors.groupingBy(Registration::getSportTypeId));

        // 按性别进一步细分
        Map<String, List<Registration>> groupKeyMap = new HashMap<>();
        registrationsBySport.forEach((sportId, regs) -> {
	            // 获取所有相关运动员信息
	            List<Long> athleteIds = regs.stream()
	                    .map(Registration::getAthleteId)
	                    .filter(Objects::nonNull)
	                    .distinct()
	                    .collect(Collectors.toList());
	            Map<Long, String> athleteGenderMap = athleteRepository.findAllById(athleteIds).stream()
	                    .collect(Collectors.toMap(Athlete::getId, Athlete::getGender));

	            Map<String, List<Registration>> byGender = regs.stream()
	                    .collect(Collectors.groupingBy(r -> {
	                        if (r.getAthleteId() != null) {
	                            return athleteGenderMap.getOrDefault(r.getAthleteId(), "Unknown");
	                        }
	                        return r.getGroup() != null ? r.getGroup() : "Unknown";
	                    }));
            byGender.forEach((gender, list) -> {
                String key = sportId + "_" + gender;
                groupKeyMap.put(key, list);
            });
        });

        // ========== 第二阶段: 时间槽分配 ==========
        List<Schedule> generatedSchedules = new ArrayList<>();
        LocalDate currentDate = startDate;
        LocalTime currentTime = dailyStartTime;
        Venue currentVenue = venues.get(0);
        int venueIndex = 0;

        // 按项目排序(决赛优先)
        List<String> sortedGroupKeys = new ArrayList<>(groupKeyMap.keySet());
        // 可以根据项目重要性排序

        for (String groupKey : sortedGroupKeys) {
            List<Registration> groupRegs = groupKeyMap.get(groupKey);
            String[] parts = groupKey.split("_");
            Long sportId = Long.parseLong(parts[0]);
            String gender = parts[1];

            SportType sportType = sportTypeRepository.findById(sportId)
                    .orElseThrow(() -> new RuntimeException("运动项目不存在"));

            // 计算需要的组数(每组最多8人)
            int groupSize = sportType.getGroupSize() != null ? sportType.getGroupSize() : 8;
            int totalGroups = (int) Math.ceil((double) groupRegs.size() / groupSize);

            // 为每组生成赛程
            for (int groupNum = 1; groupNum <= totalGroups; groupNum++) {
                // 预估比赛时长(分钟)
                int estimatedDuration = estimateDuration(sportType);
                LocalTime endTime = currentTime.plusMinutes(estimatedDuration);

                // 检查是否超出当天时间
                if (endTime.isAfter(dailyEndTime)) {
                    // 换到第二天
                    currentDate = currentDate.plusDays(1);
                    if (currentDate.isAfter(endDate)) {
                        throw new RuntimeException("赛程时间不足，无法安排所有比赛");
                    }
                    currentTime = dailyStartTime;
                    endTime = currentTime.plusMinutes(estimatedDuration);
                }

                // 检查场地是否可用
                if (!isVenueAvailable(currentVenue.getId(), currentDate, currentTime, endTime)) {
                    // 尝试下一个场地
                    venueIndex = (venueIndex + 1) % venues.size();
                    currentVenue = venues.get(venueIndex);
                    
                    // 如果所有场地都不可用，推迟时间
                    if (venueIndex == 0) {
                        currentTime = endTime;
                        currentDate = currentDate.plusDays(currentTime.isAfter(dailyEndTime) ? 1 : 0);
                        if (currentDate.isAfter(endDate)) {
                            throw new RuntimeException("赛程时间不足，无法安排所有比赛");
                        }
                    }
                }

                // 确定轮次类型
                String roundType = determineRoundType(totalGroups, groupNum, sportType);

                // 创建赛程
                Schedule schedule = Schedule.builder()
                        .event(event)
                        .sportType(sportType)
                        .venue(currentVenue)
                        .name(generateScheduleName(sportType, gender, roundType, groupNum))
                        .roundType(roundType)
                        .groupNumber(groupNum)
                        .date(currentDate)
                        .startTime(currentTime)
                        .endTime(endTime)
                        .groupName(gender.equals("M") ? "男子组" : "女子组")
                        .status("SCHEDULED")
                        .participantCount(Math.min(groupSize, groupRegs.size() - (groupNum - 1) * groupSize))
                        .build();

                generatedSchedules.add(scheduleRepository.save(schedule));

                // 更新时间指针
                currentTime = endTime.plusMinutes(5); // 间隔5分钟
            }
        }

        log.info("智能赛程编排完成，共生成 {} 场比赛", generatedSchedules.size());
        return generatedSchedules;
    }

    /**
     * 预估比赛时长(分钟)
     */
    private int estimateDuration(SportType sportType) {
        // 根据项目类型预估时长
        String category = sportType.getCategory();
        int groupSize = sportType.getGroupSize() != null ? sportType.getGroupSize() : 8;
        
        // 基础时长
        int baseDuration = switch (category) {
            case "田径" -> {
                if (sportType.getIsTimed()) {
                    // 短跑项目: 每组约15分钟
                    yield 15;
                } else {
                    // 跳跃/投掷: 每组约30分钟
                    yield 30;
                }
            }
            case "游泳" -> 20; // 游泳每组约20分钟
            case "球类" -> 60; // 球类比赛每场约60分钟
            default -> 20;
        };

        // 根据人数调整
        return baseDuration + (groupSize - 8) * 2;
    }

    /**
     * 确定轮次类型
     */
    private String determineRoundType(int totalGroups, int currentGroup, SportType sportType) {
        // 简化逻辑: 
        // 如果只有1组，直接决赛
        // 如果有2-3组，全部预赛
        // 如果有4组以上，最后2组半决赛，其余预赛
        
        if (totalGroups == 1) {
            return "FINAL";
        } else if (totalGroups <= 3) {
            return "PRELIMINARY";
        } else {
            if (currentGroup > totalGroups - 2) {
                return "SEMI_FINAL";
            } else {
                return "PRELIMINARY";
            }
        }
    }

    /**
     * 生成赛程名称
     */
    private String generateScheduleName(SportType sportType, String gender, String roundType, int groupNum) {
        String genderName = gender.equals("M") ? "男子" : "女子";
        String roundName = switch (roundType) {
            case "FINAL" -> "决赛";
            case "SEMI_FINAL" -> "半决赛";
            case "PRELIMINARY" -> "预赛";
            default -> "比赛";
        };
        
        return String.format("%s%s%s第%d组", genderName, sportType.getName(), roundName, groupNum);
    }

    /**
     * 检查场地是否可用
     */
    private boolean isVenueAvailable(Long venueId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        // 检查是否有时间冲突
        List<Schedule> conflicts = scheduleRepository.findConflictingSchedules(
                venueId, date, startTime.toString(), endTime.toString());
        return conflicts.isEmpty();
    }

    /**
     * 手动创建赛程
     */
    @Transactional
    public Schedule createSchedule(Schedule schedule) {
        // 验证时间冲突
        if (!isVenueAvailable(schedule.getVenue().getId(), schedule.getDate(), 
                schedule.getStartTime(), schedule.getEndTime())) {
            throw new RuntimeException("该时间段场地已被占用");
        }

        schedule.setStatus("SCHEDULED");
        schedule = scheduleRepository.save(schedule);
        
        log.info("创建赛程: {}", schedule.getName());
        return schedule;
    }

    /**
     * 更新赛程
     */
    @Transactional
    public Schedule updateSchedule(Long id, Schedule scheduleDetails) {
        Schedule schedule = getScheduleById(id);
        
        schedule.setName(scheduleDetails.getName());
        schedule.setRoundType(scheduleDetails.getRoundType());
        schedule.setGroupNumber(scheduleDetails.getGroupNumber());
        schedule.setDate(scheduleDetails.getDate());
        schedule.setStartTime(scheduleDetails.getStartTime());
        schedule.setEndTime(scheduleDetails.getEndTime());
        schedule.setGroupName(scheduleDetails.getGroupName());
        schedule.setRemark(scheduleDetails.getRemark());
        
        // 如果更改了场地，检查冲突
        if (!schedule.getVenue().getId().equals(scheduleDetails.getVenue().getId())) {
            if (!isVenueAvailable(scheduleDetails.getVenue().getId(), scheduleDetails.getDate(),
                    scheduleDetails.getStartTime(), scheduleDetails.getEndTime())) {
                throw new RuntimeException("该时间段场地已被占用");
            }
            schedule.setVenue(scheduleDetails.getVenue());
        }
        
        return scheduleRepository.save(schedule);
    }

    /**
     * 更新赛程状态
     */
    @Transactional
    public Schedule updateStatus(Long id, String status) {
        Schedule schedule = getScheduleById(id);
        schedule.setStatus(status);
        return scheduleRepository.save(schedule);
    }

    /**
     * 删除赛程
     */
    @Transactional
    public void deleteSchedule(Long id) {
        Schedule schedule = getScheduleById(id);
        
        if ("ONGOING".equals(schedule.getStatus()) || "COMPLETED".equals(schedule.getStatus())) {
            throw new RuntimeException("进行中或已完成的赛程不能删除");
        }
        
        scheduleRepository.delete(schedule);
        log.info("删除赛程: {}", schedule.getName());
    }

    /**
     * 获取赛程详情
     */
    public Schedule getScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("赛程不存在"));
    }

    /**
     * 根据赛事获取赛程列表
     */
    public List<Schedule> getSchedulesByEvent(Long eventId) {
        return scheduleRepository.findByEventId(eventId);
    }

    /**
     * 根据日期获取赛程
     */
    public List<Schedule> getSchedulesByDate(LocalDate date) {
        return scheduleRepository.findByDateOrderByStartTimeAsc(date);
    }

    /**
     * 获取赛事日期列表
     */
    public List<LocalDate> getEventDates(Long eventId) {
        return scheduleRepository.findDistinctDatesByEventId(eventId);
    }
}
