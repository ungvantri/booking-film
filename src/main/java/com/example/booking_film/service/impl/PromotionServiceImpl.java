package com.example.booking_film.service.impl;

import com.example.booking_film.customexception.BadRequestException;
import com.example.booking_film.customexception.DuplicateResourceException;
import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.dto.response.PromotionDTO;
import com.example.booking_film.entity.Promotion;
import com.example.booking_film.enums.DiscountType;
import com.example.booking_film.mapper.PromotionMapper;
import com.example.booking_film.repository.PromotionRepository;
import com.example.booking_film.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    public PromotionDTO applyPromotion(String code) {
        Promotion promotion = promotionRepository.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("Khong tim thay khuyen mai co ma code:" + code));
        if(!promotion.getIsActive()) {
            throw new BadRequestException("Mã khuyến mãi " + code + " chưa được kích hoạt");
        }
        LocalDate now = LocalDate.now();
        if(now.isBefore(promotion.getStartDate()) || now.isAfter(promotion.getEndDate())) {
            throw new BadRequestException("Mã khuyến mãi " + code + " đã hết hạn sử dụng");
        }
        int used = promotion.getUsedCount() != null ? promotion.getUsedCount() : 0;
        if(promotion.getUsageLimit() != null && used >= promotion.getUsageLimit()) {
            throw new BadRequestException("Mã khuyến mãi " + code + " đã hết lượt sử dụng");
        }
        incrementUsedCount(promotion.getId(), Long.valueOf(promotion.getUsageLimit()));
        promotion.setUsedCount(used + 1);
        return promotionMapper.toDTO(promotion);
    }

    @Override
    public List<PromotionDTO> getAllPromotions() {
        List<Promotion> promotions = promotionRepository.findAll();
        return promotionMapper.toDTO(promotions);
    }

    @Override
    @Transactional
    public PromotionDTO createPromotion(PromotionDTO request) {
        if(promotionRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Khuyến mãi đã tồn tại");
        }
        validatePromotionDatesAndDiscount(request);
        Promotion promotion = promotionMapper.toEntity(request);
        promotion.setId(null);
        if (promotion.getUsedCount() == null) promotion.setUsedCount(0);
        if (promotion.getIsActive() == null) promotion.setIsActive(true);
        Promotion saved = promotionRepository.save(promotion);
        return promotionMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public PromotionDTO updatePromotion(Long id, PromotionDTO request) {
        Promotion promotion = promotionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã khuyến mãi có id: " + id));
        if (!promotion.getCode().equalsIgnoreCase(request.getCode()) && promotionRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Mã khuyến mãi " + request.getCode() + " đã tồn tại");
        }
        validatePromotionDatesAndDiscount(request);
        promotion.setCode(request.getCode());
        promotion.setDescription(request.getDescription());
        promotion.setDiscountType(request.getDiscountType());
        promotion.setDiscountValue(request.getDiscountValue());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setUsageLimit(request.getUsageLimit());
        if (request.getIsActive() != null) {
            promotion.setIsActive(request.getIsActive());
        }
        Promotion saved = promotionRepository.save(promotion);
        return promotionMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public void deletePromotion(Long id) {
        if(!promotionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy mã khuyến mãi có id: " + id);
        }
        promotionRepository.deleteById(id);
    }

    @Override
    public void incrementUsedCount(Long id, Long usageLimit) {
        int row = promotionRepository.incrementUsedCount(id);
        if (row == 0) {
            throw new BadRequestException("Mã khuyến mãi vừa hết lượt sử dụng!");
        }
    }

    @Override
    public void decrementUsedCount(Long id) {
        int row = promotionRepository.decrementUsedCount(id);
        if (row == 0) {
            throw new BadRequestException("Hoàn trả mã khuyến mãi không thành công!");
        }
    }

    private void validatePromotionDatesAndDiscount(PromotionDTO request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Ngày bắt đầu không thể sau ngày kết thúc");
        }
        if (request.getDiscountType() == DiscountType.PERCENT) {
            if (request.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0 || request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestException("Giảm giá theo phần trăm phải nằm trong khoảng từ 1% đến 100%");
            }
        }
    }
}
