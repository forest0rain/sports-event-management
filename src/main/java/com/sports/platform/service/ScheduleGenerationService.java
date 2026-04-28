package com.sports.platform.service;

import com.sports.platform.entity.Event;
import com.sports.platform.entity.Schedule;
import com.sports.platform.entity.Venue;
import com.sports.platform.repository.EventRepository;
import com.sports.platform.repository.ScheduleRepository;
import com.sports.platform.repository.SportTypeRepository;
import com.sports.platform.repository.VenueRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能赛程编排服务
 * 使用贪心算法 + 局部搜索优化实现高效赛程编排
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleGenerationService {

    private final EventRepository eventRepository;
    private final SportTypeRepository sportTypeRepository;
    private final ScheduleRepository scheduleRepository;
    private final VenueRepository venueRepository;

    // 算法参数（优化后适合小规模数据）
    private static final int MAX_ITERATIONS = 100;           // 最大迭代次数
    private static final int MAX_MATCHES_PER_DAY = 10;       // 每场地每天最大场次

    // 时间段定义
    private static final List<LocalTime[]> TIME_SLOTS = Arrays.asList(
        new LocalTime[]{LocalTime.of(8, 0), LocalTime.of(10, 0)},
        new LocalTime[]{LocalTime.of(10, 30), LocalTime.of(12, 30)},
        new LocalTime[]{LocalTime.of(14, 0), LocalTime.of(16, 0)},
        new LocalTime[]{LocalTime.of(16, 30), LocalTime.of(18, 30)},
        new LocalTime[]{LocalTime.of(19, 0), LocalTime.of(21, 0)}
    );

    /**
     * 为赛事生成智能赛程
     */
    public GenerationResult generateSchedule(Long eventId) {
        log.info("开始为赛事 {} 生成智能赛程", eventId);

        try {
            Optional<Event> eventOpt = eventRepository.findById(eventId);
            if (eventOpt.isEmpty()) {
                return GenerationResult.fail("赛事不存在");
            }

            Event event = eventOpt.get();
            if (!"PUBLISHED".equals(event.getStatus())) {
                return GenerationResult.fail("只能为已发布的赛事生成赛程");
            }

            // 获取赛事期间的所有日期
            List<LocalDate> eventDates = getDateRange(event.getStartDate(), event.getEndDate());
            if (eventDates.isEmpty()) {
                return GenerationResult.fail("赛事日期范围无效");
            }

            // 获取该赛事下的所有赛程
            List<Schedule> existingSchedules = scheduleRepository.findByEventId(eventId);
            if (existingSchedules.isEmpty()) {
                return GenerationResult.fail("请先添加赛程安排");
            }

            // 获取所有场地
            List<Venue> venues = venueRepository.findAll();
            if (venues.isEmpty()) {
                return GenerationResult.fail("请先添加场地信息");
            }

            // 过滤出待编排的赛程
            List<Schedule> pendingSchedules = existingSchedules.stream()
                .filter(s -> "PENDING".equals(s.getStatus()))
                .collect(Collectors.toList());

            if (pendingSchedules.isEmpty()) {
                return GenerationResult.fail("所有赛程已编排完成");
            }

            log.info("待编排赛程数: {}, 场地数: {}, 日期数: {}", 
                pendingSchedules.size(), venues.size(), eventDates.size());

            // 使用贪心算法编排
            ScheduleOptimizationResult result = greedyScheduleGeneration(
                eventDates, pendingSchedules, venues
            );

            // 保存结果
            saveOptimizedSchedules(result.getOptimizedSchedules());

            log.info("赛程生成完成，优化了 {} 个赛程，冲突数: {}", 
                result.getOptimizedSchedules().size(), result.getConflicts());

            // 计算得分 (100 - 冲突数 * 10, 最高100分)
            double score = Math.max(0, 100 - result.getConflicts() * 10);

            return GenerationResult.success(
                "智能编排完成，共编排 " + result.getOptimizedSchedules().size() + " 个赛程",
                result.getOptimizedSchedules().size(),
                score,
                result.getConflicts()
            );

        } catch (Exception e) {
            log.error("编排过程出错", e);
            return GenerationResult.fail("编排失败: " + e.getMessage());
        }
    }

    /**
     * 贪心算法生成赛程
     */
    private ScheduleOptimizationResult greedyScheduleGeneration(
            List<LocalDate> eventDates,
            List<Schedule> schedules,
            List<Venue> venues) {

        List<OptimizedScheduleSlot> optimizedSchedules = new ArrayList<>();
        
        // 场地使用记录: date -> venueId -> List<LocalTime[]>
        Map<LocalDate, Map<Long, List<LocalTime[]>>> venueUsage = new HashMap<>();
        for (LocalDate date : eventDates) {
            venueUsage.put(date, new HashMap<>());
            for (Venue venue : venues) {
                venueUsage.get(date).put(venue.getId(), new ArrayList<>());
            }
        }

        // 运动员参赛记录: sportTypeId -> List<已参赛时间>
        Map<Long, List<LocalTime>> athleteSchedules = new HashMap<>();

        int conflicts = 0;

        for (Schedule schedule : schedules) {
            Long sportTypeId = schedule.getSportType() != null ? schedule.getSportType().getId() : 0L;
            OptimizedScheduleSlot bestSlot = null;
            int minConflicts = Integer.MAX_VALUE;

            // 遍历所有日期、时间段、场地，找最优位置
            outerLoop:
            for (LocalDate date : eventDates) {
                for (LocalTime[] timeSlot : TIME_SLOTS) {
                    for (Venue venue : venues) {
                        List<LocalTime[]> usedSlots = venueUsage.get(date).get(venue.getId());
                        
                        // 检查该场地此时段是否已被占用
                        if (usedSlots != null) {
                            for (LocalTime[] used : usedSlots) {
                                if (timesOverlap(timeSlot[0], timeSlot[1], used[0], used[1])) {
                                    continue;
                                }
                            }
                        }

                        // 计算当前选择的冲突数
                        int currentConflicts = calculateConflicts(
                            date, timeSlot, venue.getId(),
                            venueUsage, athleteSchedules, sportTypeId
                        );

                        if (currentConflicts < minConflicts) {
                            minConflicts = currentConflicts;
                            bestSlot = new OptimizedScheduleSlot();
                            bestSlot.setScheduleId(schedule.getId());
                            bestSlot.setEventId(schedule.getEvent() != null ? schedule.getEvent().getId() : null);
                            bestSlot.setSportTypeId(sportTypeId);
                            bestSlot.setDate(date);
                            bestSlot.setStartTime(timeSlot[0]);
                            bestSlot.setEndTime(timeSlot[1]);
                            bestSlot.setVenueId(venue.getId());

                            // 如果没有冲突，直接选择
                            if (currentConflicts == 0) {
                                break outerLoop;
                            }
                        }
                    }
                }
            }

            // 如果找到合适的槽位，添加到结果
            if (bestSlot != null) {
                optimizedSchedules.add(bestSlot);
                conflicts += minConflicts;

                // 更新场地使用记录
                venueUsage.get(bestSlot.getDate())
                    .computeIfAbsent(bestSlot.getVenueId(), k -> new ArrayList<>())
                    .add(new LocalTime[]{bestSlot.getStartTime(), bestSlot.getEndTime()});

                // 更新运动员参赛记录
                athleteSchedules.computeIfAbsent(sportTypeId, k -> new ArrayList<>())
                    .add(bestSlot.getEndTime());
            }
        }

        return new ScheduleOptimizationResult(optimizedSchedules, 0, conflicts);
    }

    /**
     * 计算选择某个槽位的冲突数
     */
    private int calculateConflicts(
            LocalDate date,
            LocalTime[] timeSlot,
            Long venueId,
            Map<LocalDate, Map<Long, List<LocalTime[]>>> venueUsage,
            Map<Long, List<LocalTime>> athleteSchedules,
            Long sportTypeId) {

        int conflicts = 0;

        // 1. 运动员休息时间不足检查
        List<LocalTime> athleteTimes = athleteSchedules.getOrDefault(sportTypeId, new ArrayList<>());
        for (LocalTime prevEnd : athleteTimes) {
            long hoursBetween = Duration.between(prevEnd, timeSlot[0]).toHours();
            if (hoursBetween >= 0 && hoursBetween < 2) {
                conflicts += 3; // 休息不足惩罚
            }
        }

        // 2. 检查场地每天场次上限
        List<LocalTime[]> usedSlots = venueUsage.get(date).get(venueId);
        if (usedSlots != null && usedSlots.size() >= MAX_MATCHES_PER_DAY) {
            conflicts += 5;
        }

        return conflicts;
    }

    /**
     * 检查时间段是否重叠
     */
    private boolean timesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return !(end1.isBefore(start2) || end2.isBefore(start1));
    }

    /**
     * 保存优化后的赛程
     */
    private void saveOptimizedSchedules(List<OptimizedScheduleSlot> optimizedSlots) {
        for (OptimizedScheduleSlot slot : optimizedSlots) {
            Optional<Schedule> scheduleOpt = scheduleRepository.findById(slot.getScheduleId());
            if (scheduleOpt.isPresent()) {
                Schedule schedule = scheduleOpt.get();
                schedule.setDate(slot.getDate());
                schedule.setStartTime(slot.getStartTime());
                schedule.setEndTime(slot.getEndTime());

                // 分配场地
                if (slot.getVenueId() != null) {
                    venueRepository.findById(slot.getVenueId()).ifPresent(schedule::setVenue);
                }

                schedule.setStatus("SCHEDULED");
                scheduleRepository.save(schedule);
            }
        }
    }

    /**
     * 获取日期范围内的所有日期
     */
    private List<LocalDate> getDateRange(LocalDate start, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            dates.add(current);
            current = current.plusDays(1);
        }
        return dates;
    }

    // ============ 内部类 ============

    @Data
    public static class OptimizedScheduleSlot {
        private Long scheduleId;
        private Long eventId;
        private Long sportTypeId;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private Long venueId;
    }

    @Data
    public static class ScheduleOptimizationResult {
        private List<OptimizedScheduleSlot> optimizedSchedules;
        private double energy;
        private int conflicts;

        public ScheduleOptimizationResult(List<OptimizedScheduleSlot> optimizedSchedules,
                                         double energy, int conflicts) {
            this.optimizedSchedules = optimizedSchedules;
            this.energy = energy;
            this.conflicts = conflicts;
        }
    }

    @Data
    public static class GenerationResult {
        private boolean success;
        private String message;
        private int optimizedCount;
        private double score;
        private int conflicts;

        public static GenerationResult success(String message, int count, double score, int conflicts) {
            GenerationResult result = new GenerationResult();
            result.setSuccess(true);
            result.setMessage(message);
            result.setOptimizedCount(count);
            result.setScore(score);
            result.setConflicts(conflicts);
            return result;
        }

        public static GenerationResult fail(String message) {
            GenerationResult result = new GenerationResult();
            result.setSuccess(false);
            result.setMessage(message);
            return result;
        }
    }
}
