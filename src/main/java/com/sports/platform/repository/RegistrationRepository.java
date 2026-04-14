package com.sports.platform.repository;

import com.sports.platform.entity.Registration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 参赛报名数据访问层
 */
@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    /**
     * 根据赛事查询报名
     */
    Page<Registration> findByEventId(Long eventId, Pageable pageable);

    /**
     * 根据运动员查询报名
     */
    Page<Registration> findByAthleteId(Long athleteId, Pageable pageable);

    /**
     * 根据状态查询报名
     */
    Page<Registration> findByStatus(String status, Pageable pageable);

    /**
     * 查询特定赛事和运动员的报名
     */
    Optional<Registration> findByEventIdAndAthleteId(Long eventId, Long athleteId);

    /**
     * 检查是否已报名
     */
    boolean existsByEventIdAndAthleteId(Long eventId, Long athleteId);

    /**
     * 查询特定赛事和项目的报名
     */
    List<Registration> findByEventIdAndSportTypeId(Long eventId, Long sportTypeId);

    /**
     * 根据用户ID查询报名列表，同时加载关联的Event和SportType
     * 解决懒加载导致的 null 问题
     */
    @Query("SELECT r FROM Registration r " +
           "LEFT JOIN FETCH r.event " +
           "LEFT JOIN FETCH r.sportType " +
           "WHERE r.user.id = :userId " +
           "ORDER BY r.createdTime DESC")
    List<Registration> findByUserIdWithDetails(@Param("userId") Long userId);

    /**
     * 根据赛事ID查询报名列表，同时加载关联的Event和SportType
     */
    @Query("SELECT r FROM Registration r " +
           "LEFT JOIN FETCH r.event " +
           "LEFT JOIN FETCH r.sportType " +
           "WHERE r.event.id = :eventId " +
           "ORDER BY r.createdTime DESC")
    List<Registration> findByEventIdWithDetails(@Param("eventId") Long eventId);

    /**
     * 统计赛事已通过的报名数量
     */
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId AND r.status = 'APPROVED'")
    Long countApprovedByEventId(@Param("eventId") Long eventId);
}
