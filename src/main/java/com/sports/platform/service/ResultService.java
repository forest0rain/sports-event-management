package com.sports.platform.service;

import com.sports.platform.entity.*;
import com.sports.platform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 成绩服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepository resultRepository;
    private final ScheduleRepository scheduleRepository;
    private final AthleteRepository athleteRepository;
    private final SportTypeRepository sportTypeRepository;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    /**
     * 录入成绩(裁判)
     */
    @Transactional
    public Result recordResult(Long scheduleId, Long athleteId, BigDecimal score, 
                                String scoreText, String status, String remark, Long refereeId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("赛程不存在"));
        
        Athlete athlete = athleteRepository.findById(athleteId)
                .orElseThrow(() -> new RuntimeException("运动员不存在"));

        User referee = userRepository.findById(refereeId)
                .orElseThrow(() -> new RuntimeException("裁判不存在"));

        // 检查是否已录入
        List<Result> existingResults = resultRepository.findByScheduleIdOrderByRankAsc(scheduleId);
        boolean exists = existingResults.stream()
                .anyMatch(r -> r.getAthlete().getId().equals(athleteId));
        
        if (exists) {
            throw new RuntimeException("该运动员成绩已录入");
        }

        // 获取报名信息
        Registration registration = registrationRepository.findByEventIdAndAthleteId(
                schedule.getEvent().getId(), athleteId).orElse(null);

        // 创建成绩记录
        Result result = Result.builder()
                .schedule(schedule)
                .athlete(athlete)
                .sportType(schedule.getSportType())
                .registration(registration)
                .score(score)
                .scoreText(scoreText)
                .status(status)
                .remark(remark)
                .referee(referee)
                .recordTime(LocalDateTime.now())
                .resultType(schedule.getRoundType())
                .build();

        // 设置参赛号码
        if (registration != null) {
            result.setBibNumber(registration.getBibNumber());
            result.setLane(registration.getLane());
        }

        result = resultRepository.save(result);
        
        log.info("录入成绩: {} - {} - {}", athlete.getName(), schedule.getName(), scoreText);
        
        return result;
    }

    /**
     * 批量录入成绩
     */
    @Transactional
    public void batchRecordResults(Long scheduleId, List<ResultInput> results, Long refereeId) {
        for (ResultInput input : results) {
            recordResult(scheduleId, input.getAthleteId(), input.getScore(), 
                    input.getScoreText(), input.getStatus(), input.getRemark(), refereeId);
        }
        
        // 计算排名
        calculateRankings(scheduleId);
    }

    /**
     * 计算排名
     */
    @Transactional
    public void calculateRankings(Long scheduleId) {
        List<Result> results = resultRepository.findByScheduleIdOrderByRankAsc(scheduleId);
        
        if (results.isEmpty()) {
            return;
        }

        Schedule schedule = results.get(0).getSchedule();
        SportType sportType = schedule.getSportType();

        // 根据项目类型排序(计时项目升序，计分项目降序)
        Comparator<Result> comparator;
        if (sportType.getIsTimed()) {
            // 计时项目: 时间越短越好(升序)
            comparator = Comparator.comparing(Result::getScore);
        } else {
            // 计分/距离项目: 分数/距离越大越好(降序)
            comparator = Comparator.comparing(Result::getScore, Comparator.reverseOrder());
        }

        // 过滤有效成绩并排序
        List<Result> validResults = results.stream()
                .filter(r -> "VALID".equals(r.getStatus()))
                .sorted(comparator)
                .toList();

        // 分配排名
        int rank = 1;
        for (Result result : validResults) {
            result.setRank(rank++);
            
            // 检查是否破纪录/个人最佳
            checkRecords(result);
            
            resultRepository.save(result);
        }

        // 处理无效成绩
        results.stream()
                .filter(r -> !"VALID".equals(r.getStatus()))
                .forEach(r -> {
                    r.setRank(null);
                    resultRepository.save(r);
                });

        log.info("计算排名完成: 赛程ID {}", scheduleId);
    }

    /**
     * 检查是否破纪录或个人最佳
     */
    private void checkRecords(Result result) {
        Athlete athlete = result.getAthlete();
        SportType sportType = result.getSportType();

        // 查询该运动员此项目的最佳成绩
        List<Result> bestResults = resultRepository.findBestResultsByAthleteAndSport(
                athlete.getId(), sportType.getId());

        if (bestResults.isEmpty()) {
            // 首次参赛，即为个人最佳
            result.setIsPersonalBest(true);
        } else {
            Result previousBest = bestResults.get(0);
            
            // 比较成绩
            boolean isBetter;
            if (sportType.getIsTimed()) {
                // 计时项目: 时间更短更好
                isBetter = result.getScore().compareTo(previousBest.getScore()) < 0;
            } else {
                // 计分/距离项目: 分数/距离更大更好
                isBetter = result.getScore().compareTo(previousBest.getScore()) > 0;
            }
            
            if (isBetter) {
                result.setIsPersonalBest(true);
            }
        }

        // TODO: 检查是否破纪录(需要维护纪录表)
        // 这里简化处理，可以根据实际情况扩展
    }

    /**
     * 更新成绩
     */
    @Transactional
    public Result updateResult(Long id, BigDecimal score, String scoreText, String status, String remark) {
        Result result = getResultById(id);
        
        result.setScore(score);
        result.setScoreText(scoreText);
        result.setStatus(status);
        result.setRemark(remark);
        
        // 重新计算排名
        resultRepository.save(result);
        calculateRankings(result.getSchedule().getId());
        
        return result;
    }

    /**
     * 删除成绩
     */
    @Transactional
    public void deleteResult(Long id) {
        Result result = getResultById(id);
        Long scheduleId = result.getSchedule().getId();
        
        resultRepository.delete(result);
        
        // 重新计算排名
        calculateRankings(scheduleId);
        
        log.info("删除成绩: {}", id);
    }

    /**
     * 获取成绩详情
     */
    public Result getResultById(Long id) {
        return resultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("成绩记录不存在"));
    }

    /**
     * 获取赛程成绩列表
     */
    public List<Result> getResultsBySchedule(Long scheduleId) {
        return resultRepository.findByScheduleIdOrderByRankAsc(scheduleId);
    }

    /**
     * 获取运动员成绩历史
     */
    public Page<Result> getResultsByAthlete(Long athleteId, Pageable pageable) {
        return resultRepository.findByAthleteIdOrderByCreatedTimeDesc(athleteId, pageable);
    }

    /**
     * 获取项目排名
     */
    public List<Result> getSportTypeRanking(Long sportTypeId) {
        return resultRepository.findRankingBySportType(sportTypeId);
    }

    /**
     * 获取运动员的破纪录成绩
     */
    public List<Result> getRecordResults(Long athleteId) {
        return resultRepository.findRecordResultsByAthlete(athleteId);
    }

    /**
     * 获取破纪录统计
     */
    public List<Object[]> getRecordStatistics() {
        return resultRepository.countByRecordType();
    }

    /**
     * 获取成绩统计分析数据
     */
    public ResultStatistics getStatistics(Long eventId) {
        ResultStatistics stats = new ResultStatistics();
        
        // 统计破纪录次数
        stats.setRecordStats(resultRepository.countByRecordType());
        
        // TODO: 更多统计数据
        
        return stats;
    }

    /**
     * 成绩输入DTO
     */
    public static class ResultInput {
        private Long athleteId;
        private BigDecimal score;
        private String scoreText;
        private String status;
        private String remark;

        // Getters and Setters
        public Long getAthleteId() { return athleteId; }
        public void setAthleteId(Long athleteId) { this.athleteId = athleteId; }
        public BigDecimal getScore() { return score; }
        public void setScore(BigDecimal score) { this.score = score; }
        public String getScoreText() { return scoreText; }
        public void setScoreText(String scoreText) { this.scoreText = scoreText; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    /**
     * 成绩统计DTO
     */
    public static class ResultStatistics {
        private List<Object[]> recordStats;

        public List<Object[]> getRecordStats() { return recordStats; }
        public void setRecordStats(List<Object[]> recordStats) { this.recordStats = recordStats; }
    }
}
