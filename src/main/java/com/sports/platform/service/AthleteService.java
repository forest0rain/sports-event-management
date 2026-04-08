package com.sports.platform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sports.platform.entity.Athlete;
import com.sports.platform.entity.User;
import com.sports.platform.repository.AthleteRepository;
import com.sports.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运动员服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AthleteService {

    private final AthleteRepository athleteRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * 创建运动员档案
     */
    @Transactional
    public Athlete createAthlete(Athlete athlete, Long userId) {
        // 如果关联用户，检查用户是否已有运动员档案
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            if (athleteRepository.findByUserId(userId).isPresent()) {
                throw new RuntimeException("该用户已有运动员档案");
            }
            
            athlete.setUser(user);
        }

        // 检查身份证号是否已存在
        if (athlete.getIdCard() != null && athleteRepository.findByIdCard(athlete.getIdCard()).isPresent()) {
            throw new RuntimeException("身份证号已存在");
        }

        // 自动计算年龄段
        athlete.setAgeGroup(athlete.determineAgeGroup());
        
        athlete = athleteRepository.save(athlete);
        log.info("创建运动员档案成功: {}", athlete.getName());
        
        return athlete;
    }

    /**
     * 更新运动员信息
     */
    @Transactional
    public Athlete updateAthlete(Long id, Athlete athleteDetails) {
        Athlete athlete = getAthleteById(id);
        
        athlete.setName(athleteDetails.getName());
        athlete.setGender(athleteDetails.getGender());
        athlete.setBirthDate(athleteDetails.getBirthDate());
        athlete.setNationality(athleteDetails.getNationality());
        athlete.setProvince(athleteDetails.getProvince());
        athlete.setCity(athleteDetails.getCity());
        athlete.setOrganization(athleteDetails.getOrganization());
        athlete.setCoach(athleteDetails.getCoach());
        athlete.setPhone(athleteDetails.getPhone());
        athlete.setEmail(athleteDetails.getEmail());
        athlete.setEmergencyContact(athleteDetails.getEmergencyContact());
        athlete.setEmergencyPhone(athleteDetails.getEmergencyPhone());
        athlete.setAvatar(athleteDetails.getAvatar());
        athlete.setBio(athleteDetails.getBio());
        athlete.setHeight(athleteDetails.getHeight());
        athlete.setWeight(athleteDetails.getWeight());
        
        // 更新年龄段
        athlete.setAgeGroup(athlete.determineAgeGroup());
        
        return athleteRepository.save(athlete);
    }

    /**
     * 更新运动员特长
     */
    @Transactional
    public Athlete updateSpecialties(Long id, List<String> specialties) {
        Athlete athlete = getAthleteById(id);
        
        try {
            athlete.setSpecialties(objectMapper.writeValueAsString(specialties));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("特长数据格式错误");
        }
        
        return athleteRepository.save(athlete);
    }

    /**
     * 更新技术特点
     */
    @Transactional
    public Athlete updateTechnicalFeatures(Long id, Map<String, String> features) {
        Athlete athlete = getAthleteById(id);
        
        try {
            athlete.setTechnicalFeatures(objectMapper.writeValueAsString(features));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("技术特点数据格式错误");
        }
        
        return athleteRepository.save(athlete);
    }

    /**
     * 更新历史成绩亮点
     */
    @Transactional
    public Athlete updateHighlights(Long id, List<String> highlights) {
        Athlete athlete = getAthleteById(id);
        
        try {
            athlete.setHighlights(objectMapper.writeValueAsString(highlights));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("亮点数据格式错误");
        }
        
        return athleteRepository.save(athlete);
    }

    /**
     * 更新自定义标签
     */
    @Transactional
    public Athlete updateCustomTags(Long id, List<String> tags) {
        Athlete athlete = getAthleteById(id);
        
        try {
            athlete.setCustomTags(objectMapper.writeValueAsString(tags));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("标签数据格式错误");
        }
        
        return athleteRepository.save(athlete);
    }

    /**
     * 更新个人最佳成绩
     */
    @Transactional
    public Athlete updatePersonalBests(Long id, Map<String, String> bests) {
        Athlete athlete = getAthleteById(id);
        
        try {
            athlete.setPersonalBests(objectMapper.writeValueAsString(bests));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("个人最佳成绩数据格式错误");
        }
        
        return athleteRepository.save(athlete);
    }

    /**
     * 获取运动员特长列表
     */
    public List<String> getSpecialties(Athlete athlete) {
        if (athlete.getSpecialties() == null || athlete.getSpecialties().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(athlete.getSpecialties(), new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    /**
     * 获取技术特点
     */
    public Map<String, String> getTechnicalFeatures(Athlete athlete) {
        if (athlete.getTechnicalFeatures() == null || athlete.getTechnicalFeatures().isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(athlete.getTechnicalFeatures(), new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    /**
     * 获取自定义标签
     */
    public List<String> getCustomTags(Athlete athlete) {
        if (athlete.getCustomTags() == null || athlete.getCustomTags().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(athlete.getCustomTags(), new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    /**
     * 删除运动员
     */
    @Transactional
    public void deleteAthlete(Long id) {
        Athlete athlete = getAthleteById(id);
        athleteRepository.delete(athlete);
        
        log.info("删除运动员: {}", athlete.getName());
    }

    /**
     * 获取运动员详情
     */
    public Athlete getAthleteById(Long id) {
        return athleteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("运动员不存在"));
    }

    /**
     * 根据用户ID获取运动员
     */
    public Athlete getAthleteByUserId(Long userId) {
        return athleteRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("运动员档案不存在"));
    }

    /**
     * 获取所有运动员
     */
    public Page<Athlete> getAllAthletes(Pageable pageable) {
        return athleteRepository.findAll(pageable);
    }

    /**
     * 搜索运动员
     */
    public Page<Athlete> searchAthletes(String keyword, Pageable pageable) {
        return athleteRepository.search(keyword, pageable);
    }

    /**
     * 根据性别查询
     */
    public List<Athlete> getAthletesByGender(String gender) {
        return athleteRepository.findByGender(gender);
    }

    /**
     * 根据年龄段查询
     */
    public List<Athlete> getAthletesByAgeGroup(String ageGroup) {
        return athleteRepository.findByAgeGroup(ageGroup);
    }

    /**
     * 更新参赛次数
     */
    @Transactional
    public void updateCompetitionCount(Long athleteId, int delta) {
        Athlete athlete = getAthleteById(athleteId);
        athlete.setCompetitionCount(athlete.getCompetitionCount() + delta);
        athleteRepository.save(athlete);
    }

    /**
     * 更新获奖次数
     */
    @Transactional
    public void updateAwardCount(Long athleteId, int delta) {
        Athlete athlete = getAthleteById(athleteId);
        athlete.setAwardCount(athlete.getAwardCount() + delta);
        athleteRepository.save(athlete);
    }

    /**
     * 获取年龄段统计
     */
    public List<Object[]> getAgeGroupStatistics() {
        return athleteRepository.countByAgeGroup();
    }
}
