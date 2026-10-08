package com.example.booking_film.service.impl;

import com.example.booking_film.customexception.BadRequestException;
import com.example.booking_film.customexception.DuplicateResourceException;
import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.dto.response.PromotionDTO;
import com.example.booking_film.entity.Promotion;
import com.example.booking_film.mapper.PromotionMapper;
import com.example.booking_film.repository.PromotionRepository;
import com.example.booking_film.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PromotionServiceImpl implements PromotionService {
    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private PromotionMapper promotionMapper;

    @Override
    public List<PromotionDTO> getActivePromotions() {
        LocalDate today = LocalDate.now();
        List<Promotion> promotions = promotionRepository.findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);
        return promotionMapper.toDTO(promotions);
    }

    @Override
    public PromotionDTO validatePromotion(String code) {
        Promotion promotion = promotionRepository.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("Khong tim thay khuyen mai co ma code:" + code));
        if(!promotion.getIsActive()) {
            throw new BadRequestException("Mã khuyến mãi " + code + " chưa được kích hoạt");
        }
        LocalDate now = LocalDate.now();
        if(now.isBefore(promotion.getStartDate()) || now.isAfter(promotion.getEndDate())) {
            throw new BadRequestException("Mã khuyến mãi " + code + " đã hết hạn sử dụng");
        }
        if(promotion.getUsedCount() >= promotion.getUsageLimit()) {
            throw new BadRequestException("Mã khuyến mãi " + code + " đã hết lượt sử dụng");
        }
        Promotion saved = promotionRepository.save(promotion);
        return promotionMapper.toDTO(saved);
    }

    @Override
    public List<PromotionDTO> getAllPromotions() {
        List<Promotion> promotions = promotionRepository.findAll();
        return promotionMapper.toDTO(promotions);
    }

    @Override
    public PromotionDTO createPromotion(PromotionDTO request) {
        if(promotionRepository.existsById(request.getId()) || promotionRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Khuyến mãi đã tồn tại");
        }
        Promotion promotion = promotionMapper.toEntity(request);
        Promotion saved = promotionRepository.save(promotion);
        return promotionMapper.toDTO(saved);
    }

    @Override
    public PromotionDTO updatePromotion(Long id, PromotionDTO request) {
        if(!promotionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy mã khuyến mãi có id: " + id);
        }
        Promotion promotion = promotionMapper.toEntity(request);
        promotion.setId(id);
        Promotion saved = promotionRepository.save(promotion);
        return promotionMapper.toDTO(saved);
    }

    @Override
    public void deletePromotion(Long id) {
        if(!promotionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy mã khuyến mãi có id: " + id);
        }
        promotionRepository.deleteById(id);
    }
}
