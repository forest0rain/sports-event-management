package com.sports.platform.repository;

import com.sports.platform.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 赛程安排数据访问层
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /**
     * 根据赛事查询赛程
     */
    List<Schedule> findByEventId(Long eventId);

    /**
     * 根据场地查询赛程
     */
    List<Schedule> findByVenueId(Long venueId);

    /**
     * 根据日期查询赛程
     */
    List<Schedule> findByDateOrderByStartTimeAsc(LocalDate date);

    /**
     * 根据赛事和日期查询
     */
    List<Schedule> findByEventIdAndDateOrderByStartTimeAsc(Long eventId, LocalDate date);

    /**
     * 根据运动项目查询
     */
    List<Schedule> findBySportTypeId(Long sportTypeId);

    /**
     * 根据状态查询
     */
    List<Schedule> findByStatus(String status);

    /**
     * 查询特定场地和时间段的赛程(用于冲突检测)
     */
    @Query("SELECT s FROM Schedule s WHERE s.venue.id = :venueId AND s.date = :date " +
           "AND ((s.startTime <= :endTime AND s.endTime >= :startTime) " +
           "OR (s.startTime >= :startTime AND s.startTime < :endTime))")
    List<Schedule> findConflictingSchedules(@Param("venueId") Long venueId, 
                                            @Param("date") LocalDate date,
                                            @Param("startTime") String startTime, 
                                            @Param("endTime") String endTime);

    /**
     * 查询赛事的日期列表
     */
    @Query("SELECT DISTINCT s.date FROM Schedule s WHERE s.event.id = :eventId ORDER BY s.date")
    List<LocalDate> findDistinctDatesByEventId(@Param("eventId") Long eventId);
}
