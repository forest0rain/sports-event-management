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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能赛程编排服务
 * 基于模拟退火算法实现多约束条件下的赛程优化编排
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleGenerationService {

    private final EventRepository eventRepository;
    private final SportTypeRepository sportTypeRepository;
    private final ScheduleRepository scheduleRepository;
    private final VenueRepository venueRepository;

    // 模拟退火算法参数
    private static final double INITIAL_TEMPERATURE = 10000.0;  // 初始温度
    private static final double COOLING_RATE = 0.9995;          // 冷却率
    private static final double MIN_TEMPERATURE = 1.0;         // 最低温度
    private static final int MAX_ITERATIONS = 10000;           // 每温度最大迭代次数

    // 权重系数
    private static final double W_VENUE_UTILIZATION = 30.0;    // 场地利用率权重
    private static final double W_TIME_COMPACTNESS = 20.0;     // 时间紧凑度权重
    private static final double W_CONFLICT_PENALTY = 100.0;   // 冲突惩罚权重
    private static final double W_REST_TIME = 15.0;            // 休息时间权重
    private static final double W_SATISFACTION = 25.0;         // 满意度权重

    // 约束条件
    private static final int MIN_REST_HOURS = 2;               // 最小休息时间(小时)
    private static final int MAX_MATCHES_PER_DAY = 10;        // 每场地每天最大场次

    /**
     * 为赛事生成智能赛程
     */
    public GenerationResult generateSchedule(Long eventId) {
        log.info("开始为赛事 {} 生成智能赛程", eventId);

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

        // 获取该赛事下的所有赛程（按运动项目分组）
        List<Schedule> existingSchedules = scheduleRepository.findByEventId(eventId);
        if (existingSchedules.isEmpty()) {
            return GenerationResult.fail("请先添加赛程安排");
        }

        // 获取所有场地用于分配
        List<Venue> venues = venueRepository.findAll();
        if (venues.isEmpty()) {
            return GenerationResult.fail("请先添加场地信息");
        }

        // 按运动项目分组
        Map<Long, List<Schedule>> schedulesBySport = new HashMap<>();
        for (Schedule s : existingSchedules) {
            Long sportTypeId = s.getSportType() != null ? s.getSportType().getId() : 0L;
            schedulesBySport.computeIfAbsent(sportTypeId, k -> new ArrayList<>()).add(s);
        }

        // 使用模拟退火算法优化编排
        ScheduleOptimizationResult result = simulatedAnnealingOptimization(
            event, eventDates, schedulesBySport, venues
        );

        // 保存优化后的赛程
        saveOptimizedSchedules(result.getOptimizedSchedules(), venues);

        log.info("赛程生成完成，优化了 {} 个时间段", result.getOptimizedSchedules().size());

        return GenerationResult.success(
            "智能编排完成",
            result.getOptimizedSchedules().size(),
            result.getEnergy(),
            result.getConflicts()
        );
    }

    /**
     * 模拟退火算法优化
     */
    private ScheduleOptimizationResult simulatedAnnealingOptimization(
            Event event,
            List<LocalDate> eventDates,
            Map<Long, List<Schedule>> schedulesBySport,
            List<Venue> venues) {

        // 初始化：贪婪策略生成初始解
        List<OptimizedScheduleSlot> currentSolution = greedyInitialSolution(eventDates, schedulesBySport, venues);
        double currentEnergy = calculateEnergy(currentSolution, eventDates);

        List<OptimizedScheduleSlot> bestSolution = deepCopy(currentSolution);
        double bestEnergy = currentEnergy;

        double temperature = INITIAL_TEMPERATURE;
        int iteration = 0;

        log.info("初始解能量: {}, 开始模拟退火...", currentEnergy);

        while (temperature > MIN_TEMPERATURE) {
            for (int i = 0; i < MAX_ITERATIONS; i++) {
                iteration++;

                // 生成邻域解（交换或移动）
                List<OptimizedScheduleSlot> neighborSolution = generateNeighbor(currentSolution);
                double neighborEnergy = calculateEnergy(neighborSolution, eventDates);

                // Metropolis接受准则
                double delta = neighborEnergy - currentEnergy;
                if (delta < 0 || Math.random() < Math.exp(-delta / temperature)) {
                    currentSolution = neighborSolution;
                    currentEnergy = neighborEnergy;

                    // 更新最优解
                    if (currentEnergy < bestEnergy) {
                        bestSolution = deepCopy(currentSolution);
                        bestEnergy = currentEnergy;
                    }
                }
            }

            // 降温
            temperature *= COOLING_RATE;

            if (iteration % 1000 == 0) {
                log.info("迭代 {} 次, 当前温度: {}, 当前能量: {}, 最优能量: {}",
                    iteration, String.format("%.2f", temperature),
                    String.format("%.2f", currentEnergy),
                    String.format("%.2f", bestEnergy));
            }
        }

        log.info("模拟退火完成, 总迭代: {}, 最优能量: {}", iteration, bestEnergy);

        // 统计冲突数
        int conflicts = countConflicts(bestSolution);

        return new ScheduleOptimizationResult(bestSolution, bestEnergy, conflicts);
    }

    /**
     * 贪婪策略生成初始解
     */
    private List<OptimizedScheduleSlot> greedyInitialSolution(
            List<LocalDate> eventDates,
            Map<Long, List<Schedule>> schedulesBySport,
            List<Venue> venues) {

        List<OptimizedScheduleSlot> slots = new ArrayList<>();
        Map<LocalDate, Map<Long, Integer>> venueUsageByDate = new HashMap<>(); // 按日期统计场地使用

        // 定义每天的时间段 (8:00-12:00, 14:00-18:00, 19:00-22:00)
        List<LocalTime[]> timeSlots = Arrays.asList(
            new LocalTime[]{LocalTime.of(8, 0), LocalTime.of(12, 0)},
            new LocalTime[]{LocalTime.of(14, 0), LocalTime.of(18, 0)},
            new LocalTime[]{LocalTime.of(19, 0), LocalTime.of(22, 0)}
        );

        int venueIndex = 0;
        for (Map.Entry<Long, List<Schedule>> entry : schedulesBySport.entrySet()) {
            List<Schedule> schedules = entry.getValue();

            for (Schedule schedule : schedules) {
                LocalDate bestDate = null;
                LocalTime[] bestTimeSlot = null;
                Long bestVenueId = null;
                int minConflicts = Integer.MAX_VALUE;

                // 遍历所有日期和时间段，找最优
                for (LocalDate date : eventDates) {
                    for (LocalTime[] timeSlot : timeSlots) {
                        for (Venue venue : venues) {
                            int conflicts = calculateSlotConflicts(
                                slots, date, timeSlot, venueUsageByDate, schedule, venue.getId()
                            );

                            if (conflicts < minConflicts) {
                                minConflicts = conflicts;
                                bestDate = date;
                                bestTimeSlot = timeSlot;
                                bestVenueId = venue.getId();
                            }
                        }
                    }
                }

                if (bestDate != null && bestTimeSlot != null && bestVenueId != null) {
                    OptimizedScheduleSlot slot = new OptimizedScheduleSlot();
                    slot.setScheduleId(schedule.getId());
                    slot.setEventId(schedule.getEvent() != null ? schedule.getEvent().getId() : null);
                    slot.setSportTypeId(schedule.getSportType() != null ? schedule.getSportType().getId() : null);
                    slot.setDate(bestDate);
                    slot.setStartTime(bestTimeSlot[0]);
                    slot.setEndTime(bestTimeSlot[1]);
                    slot.setVenueId(bestVenueId);

                    slots.add(slot);

                    // 更新场地使用统计
                    venueUsageByDate.computeIfAbsent(bestDate, k -> new HashMap<>())
                        .merge(bestVenueId, 1, Integer::sum);
                }
            }
        }

        return slots;
    }

    /**
     * 生成邻域解（邻域移动）
     */
    private List<OptimizedScheduleSlot> generateNeighbor(List<OptimizedScheduleSlot> solution) {
        List<OptimizedScheduleSlot> neighbor = deepCopy(solution);
        if (neighbor.isEmpty()) return neighbor;

        Random random = new Random();
        int operation = random.nextInt(3);

        switch (operation) {
            case 0: // 交换两个赛程的时间
                if (neighbor.size() >= 2) {
                    int i = random.nextInt(neighbor.size());
                    int j = random.nextInt(neighbor.size());
                    if (i != j) {
                        OptimizedScheduleSlot temp = neighbor.get(i);
                        neighbor.set(i, neighbor.get(j));
                        neighbor.set(j, temp);
                    }
                }
                break;

            case 1: // 移动一个赛程到不同日期
                if (neighbor.size() >= 1) {
                    int idx = random.nextInt(neighbor.size());
                    OptimizedScheduleSlot slot = neighbor.get(idx);
                    // 简单移动到下一天或前一天
                    if (random.nextBoolean()) {
                        slot.setDate(slot.getDate().plusDays(1));
                    } else {
                        slot.setDate(slot.getDate().minusDays(1));
                    }
                }
                break;

            case 2: // 调整时间段
                if (neighbor.size() >= 1) {
                    int idx = random.nextInt(neighbor.size());
                    OptimizedScheduleSlot slot = neighbor.get(idx);
                    // 切换到相邻时间段
                    LocalTime currentStart = slot.getStartTime();
                    if (currentStart.equals(LocalTime.of(8, 0))) {
                        slot.setStartTime(LocalTime.of(14, 0));
                        slot.setEndTime(LocalTime.of(18, 0));
                    } else if (currentStart.equals(LocalTime.of(14, 0))) {
                        slot.setStartTime(LocalTime.of(19, 0));
                        slot.setEndTime(LocalTime.of(22, 0));
                    } else {
                        slot.setStartTime(LocalTime.of(8, 0));
                        slot.setEndTime(LocalTime.of(12, 0));
                    }
                }
                break;
        }

        return neighbor;
    }

    /**
     * 计算解的能量值（目标函数）
     * 能量越低，解越好
     */
    private double calculateEnergy(List<OptimizedScheduleSlot> solution, List<LocalDate> eventDates) {
        if (solution.isEmpty()) return 0;

        double energy = 0;

        // 1. 场地利用率 (越高越好，取负值)
        double venueUtilization = calculateVenueUtilization(solution, eventDates);
        energy -= W_VENUE_UTILIZATION * venueUtilization;

        // 2. 时间紧凑度 (越紧凑越好)
        double timeCompactness = calculateTimeCompactness(solution, eventDates);
        energy -= W_TIME_COMPACTNESS * timeCompactness;

        // 3. 冲突惩罚 (冲突越少越好)
        int conflicts = countConflicts(solution);
        energy += W_CONFLICT_PENALTY * conflicts;

        // 4. 休息时间满意度 (越合理越好)
        double restSatisfaction = calculateRestSatisfaction(solution);
        energy -= W_REST_TIME * restSatisfaction;

        // 5. 运动员满意度 (避免晚场、连续参赛等)
        double athleteSatisfaction = calculateAthleteSatisfaction(solution);
        energy -= W_SATISFACTION * athleteSatisfaction;

        return energy;
    }

    /**
     * 计算场地利用率
     */
    private double calculateVenueUtilization(List<OptimizedScheduleSlot> solution, List<LocalDate> eventDates) {
        if (solution.isEmpty() || eventDates.isEmpty()) return 0;

        Map<LocalDate, Long> venueUsage = new HashMap<>();
        for (OptimizedScheduleSlot slot : solution) {
            venueUsage.merge(slot.getDate(), 1L, Long::sum);
        }

        // 计算平均每天使用次数
        double totalUsage = venueUsage.values().stream().mapToLong(Long::longValue).sum();
        double avgUsage = totalUsage / eventDates.size();

        // 理想情况：每天均匀分布，使用率接近100%
        double idealUsage = 3.0; // 每个时间段理想使用
        double utilization = Math.min(1.0, avgUsage / idealUsage);

        return utilization;
    }

    /**
     * 计算时间紧凑度
     */
    private double calculateTimeCompactness(List<OptimizedScheduleSlot> solution, List<LocalDate> eventDates) {
        if (solution.isEmpty()) return 0;

        LocalDate firstDate = eventDates.get(0);
        LocalDate lastDate = eventDates.get(eventDates.size() - 1);

        int firstDayCount = 0, lastDayCount = 0;
        for (OptimizedScheduleSlot slot : solution) {
            if (slot.getDate().equals(firstDate)) firstDayCount++;
            if (slot.getDate().equals(lastDate)) lastDayCount++;
        }

        // 首尾天数应该有较多比赛，紧凑度高
        double compactness = (firstDayCount + lastDayCount) / (double) solution.size();
        return compactness;
    }

    /**
     * 统计冲突数量
     */
    private int countConflicts(List<OptimizedScheduleSlot> solution) {
        int conflicts = 0;

        // 1. 同一场地同一时间冲突
        Map<String, List<OptimizedScheduleSlot>> timeVenueSlots = new HashMap<>();
        for (OptimizedScheduleSlot slot : solution) {
            String key = slot.getDate() + "_" + slot.getStartTime() + "_" + slot.getVenueId();
            timeVenueSlots.computeIfAbsent(key, k -> new ArrayList<>()).add(slot);
        }
        for (List<OptimizedScheduleSlot> slots : timeVenueSlots.values()) {
            if (slots.size() > 1) {
                conflicts += slots.size() - 1;
            }
        }

        // 2. 运动员休息时间不足（按scheduleId分组）
        Map<Long, List<OptimizedScheduleSlot>> scheduleSlots = new HashMap<>();
        for (OptimizedScheduleSlot slot : solution) {
            scheduleSlots.computeIfAbsent(slot.getScheduleId(), k -> new ArrayList<>()).add(slot);
        }
        for (List<OptimizedScheduleSlot> slots : scheduleSlots.values()) {
            slots.sort(Comparator.comparing(OptimizedScheduleSlot::getStartTime));
            for (int i = 1; i < slots.size(); i++) {
                OptimizedScheduleSlot prev = slots.get(i - 1);
                OptimizedScheduleSlot curr = slots.get(i);
                if (prev.getDate().equals(curr.getDate())) {
                    long hoursBetween = java.time.Duration.between(
                        prev.getEndTime(), curr.getStartTime()
                    ).toHours();
                    if (hoursBetween < MIN_REST_HOURS) {
                        conflicts++;
                    }
                }
            }
        }

        return conflicts;
    }

    /**
     * 计算某个时间槽的冲突数
     */
    private int calculateSlotConflicts(
            List<OptimizedScheduleSlot> existingSlots,
            LocalDate date,
            LocalTime[] timeSlot,
            Map<LocalDate, Map<Long, Integer>> venueUsageByDate,
            Schedule newSchedule,
            Long venueId) {

        int conflicts = 0;

        // 检查场地时间冲突
        for (OptimizedScheduleSlot slot : existingSlots) {
            if (slot.getDate().equals(date) && slot.getVenueId() != null && slot.getVenueId().equals(venueId)) {
                if (timesOverlap(slot.getStartTime(), slot.getEndTime(), timeSlot[0], timeSlot[1])) {
                    conflicts += 5;
                }
            }
        }

        // 检查每天场次上限
        Map<Long, Integer> dayUsage = venueUsageByDate.getOrDefault(date, new HashMap<>());
        int totalDayUsage = dayUsage.values().stream().mapToInt(Integer::intValue).sum();
        if (totalDayUsage >= MAX_MATCHES_PER_DAY) {
            conflicts += 3;
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
     * 计算休息时间满意度
     */
    private double calculateRestSatisfaction(List<OptimizedScheduleSlot> solution) {
        if (solution.isEmpty()) return 0;

        double totalSatisfaction = 0;
        int count = 0;

        Map<LocalDate, List<OptimizedScheduleSlot>> byDate = new HashMap<>();
        for (OptimizedScheduleSlot slot : solution) {
            byDate.computeIfAbsent(slot.getDate(), k -> new ArrayList<>()).add(slot);
        }

        for (List<OptimizedScheduleSlot> daySlots : byDate.values()) {
            daySlots.sort(Comparator.comparing(OptimizedScheduleSlot::getStartTime));
            for (int i = 1; i < daySlots.size(); i++) {
                long restHours = java.time.Duration.between(
                    daySlots.get(i - 1).getEndTime(),
                    daySlots.get(i).getStartTime()
                ).toHours();

                // 2-4小时休息最理想，得分1.0
                double satisfaction = 1.0 - Math.abs(restHours - 3.0) / 5.0;
                satisfaction = Math.max(0, Math.min(1, satisfaction));
                totalSatisfaction += satisfaction;
                count++;
            }
        }

        return count > 0 ? totalSatisfaction / count : 0;
    }

    /**
     * 计算运动员满意度
     */
    private double calculateAthleteSatisfaction(List<OptimizedScheduleSlot> solution) {
        if (solution.isEmpty()) return 0;

        double satisfaction = 0;

        // 惩罚晚上太晚的比赛 (19:00后)
        int lateNightCount = 0;
        for (OptimizedScheduleSlot slot : solution) {
            if (slot.getStartTime().isAfter(LocalTime.of(20, 0))) {
                lateNightCount++;
            }
        }
        satisfaction -= lateNightCount * 0.1;

        // 奖励首日上午比赛
        for (OptimizedScheduleSlot slot : solution) {
            if (slot.getDate().equals(solution.get(0).getDate()) &&
                slot.getStartTime().isBefore(LocalTime.of(12, 0))) {
                satisfaction += 0.05;
            }
        }

        return Math.max(0, satisfaction);
    }

    /**
     * 保存优化后的赛程
     */
    private void saveOptimizedSchedules(List<OptimizedScheduleSlot> optimizedSlots, List<Venue> venues) {
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

    /**
     * 深拷贝解
     */
    private List<OptimizedScheduleSlot> deepCopy(List<OptimizedScheduleSlot> original) {
        List<OptimizedScheduleSlot> copy = new ArrayList<>();
        for (OptimizedScheduleSlot slot : original) {
            OptimizedScheduleSlot newSlot = new OptimizedScheduleSlot();
            newSlot.setScheduleId(slot.getScheduleId());
            newSlot.setEventId(slot.getEventId());
            newSlot.setSportTypeId(slot.getSportTypeId());
            newSlot.setDate(slot.getDate());
            newSlot.setStartTime(slot.getStartTime());
            newSlot.setEndTime(slot.getEndTime());
            newSlot.setVenueId(slot.getVenueId());
            copy.add(newSlot);
        }
        return copy;
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
