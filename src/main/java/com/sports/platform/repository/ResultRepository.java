package com.sports.platform.repository;

import com.sports.platform.entity.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 成绩数据访问层
 */
@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

    /**
     * 根据运动员查询成绩
     */
    Page<Result> findByAthleteIdOrderByCreatedTimeDesc(Long athleteId, Pageable pageable);

    /**
     * 根据赛程查询成绩
     */
    List<Result> findByScheduleIdOrderByRankAsc(Long scheduleId);

    /**
     * 根据运动项目查询成绩
     */
    Page<Result> findBySportTypeIdOrderByCreatedTimeDesc(Long sportTypeId, Pageable pageable);

    /**
     * 查询运动员特定项目的最佳成绩
     */
    @Query("SELECT r FROM Result r WHERE r.athlete.id = :athleteId AND r.sportType.id = :sportTypeId " +
           "AND r.status = 'VALID' ORDER BY r.score ASC")
    List<Result> findBestResultsByAthleteAndSport(@Param("athleteId") Long athleteId, 
                                                   @Param("sportTypeId") Long sportTypeId);

    /**
     * 查询项目排名
     */
    @Query("SELECT r FROM Result r WHERE r.sportType.id = :sportTypeId AND r.status = 'VALID' " +
           "ORDER BY r.score ASC")
    List<Result> findRankingBySportType(@Param("sportTypeId") Long sportTypeId);

    /**
     * 统计破纪录次数
     */
    @Query("SELECT r.recordType, COUNT(r) FROM Result r WHERE r.recordType != '无' GROUP BY r.recordType")
    List<Object[]> countByRecordType();

    /**
     * 查询运动员的破纪录成绩
     */
    @Query("SELECT r FROM Result r WHERE r.athlete.id = :athleteId AND r.recordType != '无'")
    List<Result> findRecordResultsByAthlete(@Param("athleteId") Long athleteId);

    /**
     * 统计运动员获奖次数(前三名)
     */
    @Query("SELECT COUNT(r) FROM Result r WHERE r.athlete.id = :athleteId AND r.rank <= 3")
    Long countAwardsByAthlete(@Param("athleteId") Long athleteId);

    /**
     * 统计成绩状态分布
     */
    @Query("SELECT r.status, COUNT(r) FROM Result r GROUP BY r.status")
    List<Object[]> countByResultStatus();

    /**
     * 获取获奖最多的运动员 TOP 10
     */
    @Query("SELECT a.name, COUNT(r) FROM Result r JOIN r.athlete a " +
           "WHERE r.rank <= 3 GROUP BY a.id, a.name ORDER BY COUNT(r) DESC")
    List<Object[]> countAwardsByAthletes();
}
