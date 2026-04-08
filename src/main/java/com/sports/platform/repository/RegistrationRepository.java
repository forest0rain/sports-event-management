package com.sports.platform.repository;

import com.sports.platform.entity.Registration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
     * 查询已审核通过的报名
     */
    @Query("SELECT r FROM Registration r WHERE r.event.id = :eventId AND r.status = 'APPROVED'")
    List<Registration> findApprovedByEventId(@Param("eventId") Long eventId);

    /**
     * 统计赛事报名人数
     */
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId AND r.status = 'APPROVED'")
    Long countApprovedByEventId(@Param("eventId") Long eventId);

    /**
     * 更新报名状态
     */
    @Modifying
    @Query("UPDATE Registration r SET r.status = :status WHERE r.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 根据赛事和状态统计数量
     */
    @Query("SELECT r.status, COUNT(r) FROM Registration r WHERE r.event.id = :eventId GROUP BY r.status")
    List<Object[]> countByEventIdAndStatus(@Param("eventId") Long eventId);
}
