package com.sports.platform.service;

import com.sports.platform.entity.Venue;
import com.sports.platform.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 场地管理服务
 */
@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    /**
     * 获取所有场地（分页）
     */
    public Page<Venue> getAllVenues(PageRequest pageRequest) {
        return venueRepository.findAll(pageRequest);
    }

    /**
     * 获取所有启用的场地
     */
    public List<Venue> getEnabledVenues() {
        return venueRepository.findByEnabledTrue();
    }

    /**
     * 根据类型查询场地
     */
    public List<Venue> getVenuesByType(String type) {
        return venueRepository.findByType(type);
    }

    /**
     * 搜索场地
     */
    public Page<Venue> searchVenues(String keyword, PageRequest pageRequest) {
        List<Venue> all = venueRepository.findByNameContaining(keyword);
        int start = (int) pageRequest.getOffset();
        int end = Math.min(start + pageRequest.getPageSize(), all.size());
        if (start >= all.size()) {
            return Page.empty(pageRequest);
        }
        return new org.springframework.data.domain.PageImpl<>(
                all.subList(start, end), pageRequest, all.size());
    }

    /**
     * 获取场地详情
     */
    public Venue getVenueById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("场地不存在"));
    }

    /**
     * 创建场地
     */
    @Transactional
    public Venue createVenue(Venue venue) {
        return venueRepository.save(venue);
    }

    /**
     * 更新场地
     */
    @Transactional
    public Venue updateVenue(Long id, Venue venueDetails) {
        Venue venue = getVenueById(id);
        venue.setName(venueDetails.getName());
        venue.setLocation(venueDetails.getLocation());
        venue.setDescription(venueDetails.getDescription());
        venue.setType(venueDetails.getType());
        venue.setCapacity(venueDetails.getCapacity());
        venue.setMaxConcurrentEvents(venueDetails.getMaxConcurrentEvents());
        venue.setFacilities(venueDetails.getFacilities());
        venue.setContactPerson(venueDetails.getContactPerson());
        venue.setContactPhone(venueDetails.getContactPhone());
        venue.setEnabled(venueDetails.getEnabled());
        return venueRepository.save(venue);
    }

    /**
     * 启用/禁用场地
     */
    @Transactional
    public void toggleVenueStatus(Long id) {
        Venue venue = getVenueById(id);
        venue.setEnabled(!venue.getEnabled());
        venueRepository.save(venue);
    }

    /**
     * 删除场地
     */
    @Transactional
    public void deleteVenue(Long id) {
        Venue venue = getVenueById(id);
        if (!venue.getSchedules().isEmpty()) {
            throw new RuntimeException("该场地下还有赛程安排，无法删除");
        }
        venueRepository.deleteById(id);
    }
}
