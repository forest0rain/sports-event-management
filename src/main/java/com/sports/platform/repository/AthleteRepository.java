package com.sports.platform.repository;

import com.sports.platform.entity.Athlete;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 运动员数据访问层
 */
@Repository
public interface AthleteRepository extends JpaRepository<Athlete, Long> {

    /**
     * 根据用户ID查询运动员
     */
    Optional<Athlete> findByUserId(Long userId);

    /**
     * 根据身份证号查询
     */
    Optional<Athlete> findByIdCard(String idCard);

    /**
     * 根据性别查询
     */
    List<Athlete> findByGender(String gender);

    /**
     * 根据年龄段查询
     */
    List<Athlete> findByAgeGroup(String ageGroup);

    /**
     * 根据所属单位查询
     */
    Page<Athlete> findByOrganizationContaining(String organization, Pageable pageable);

    /**
     * 搜索运动员(姓名或单位)
     */
    @Query("SELECT a FROM Athlete a WHERE a.name LIKE %:keyword% OR a.organization LIKE %:keyword%")
    Page<Athlete> search(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据特长查询(使用JSON函数)
     */
    @Query(value = "SELECT * FROM athlete WHERE JSON_CONTAINS(specialties, :specialty) = 1", nativeQuery = true)
    List<Athlete> findBySpecialty(@Param("specialty") String specialty);

    /**
     * 统计各年龄段运动员数量
     */
    @Query("SELECT a.ageGroup, COUNT(a) FROM Athlete a GROUP BY a.ageGroup")
    List<Object[]> countByAgeGroup();

    /**
     * 查询启用状态的运动员
     */
    Page<Athlete> findByEnabledTrue(Pageable pageable);

    /**
     * 统计各性别运动员数量
     */
    @Query("SELECT a.gender, COUNT(a) FROM Athlete a GROUP BY a.gender")
    List<Object[]> countByGender();
}
