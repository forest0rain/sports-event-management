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
     * 统计某赛事的已批准报名数
     */
    long countByEventIdAndStatus(Long eventId, String status);

    /**
     * 统计某赛事所有报名数
     */
    long countByEventId(Long eventId);

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
     * 根据用户ID查询报名列表（用于普通用户）
     */
    List<Registration> findByAthleteIdOrderByCreatedTimeDesc(Long athleteId);

    /**
     * 查询赛事已通过的报名列表
     */
    List<Registration> findByEventIdAndStatus(Long eventId, String status);

    /**
     * 根据用户ID查询报名列表
     */
    List<Registration> findByUserId(Long userId);

    /**
     * 根据赛事ID查询已通过的报名列表
     */
    default List<Registration> findApprovedByEventId(Long eventId) {
        return findByEventIdAndStatus(eventId, "APPROVED");
    }

    /**
     * 按状态统计赛事报名数量
     */
    @Query("SELECT r.status, COUNT(r) FROM Registration r WHERE r.eventId = :eventId GROUP BY r.status")
    List<Object[]> countByEventIdAndStatus(@Param("eventId") Long eventId);
}
