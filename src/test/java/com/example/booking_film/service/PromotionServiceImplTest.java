package com.example.booking_film.service;

import com.example.booking_film.customexception.BadRequestException;
import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.dto.response.PromotionDTO;
import com.example.booking_film.entity.Promotion;
import com.example.booking_film.mapper.PromotionMapper;
import com.example.booking_film.repository.PromotionRepository;
import com.example.booking_film.service.impl.PromotionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceImplTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private PromotionMapper promotionMapper;

    @InjectMocks
    private PromotionServiceImpl promotionService;

    @Test
    @DisplayName("applyPromotion ném ResourceNotFoundException khi mã khuyến mãi không tồn tại")
    void testApplyPromotion_NotFound() {
        String code = "INVALID_CODE";
        when(promotionRepository.findByCode(code)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                promotionService.applyPromotion(code)
        );
    }

    @Test
    @DisplayName("applyPromotion ném BadRequestException khi mã khuyến mãi chưa kích hoạt")
    void testApplyPromotion_NotActive() {
        String code = "INACTIVE_CODE";
        Promotion promotion = new Promotion();
        promotion.setCode(code);
        promotion.setIsActive(false);

        when(promotionRepository.findByCode(code)).thenReturn(Optional.of(promotion));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                promotionService.applyPromotion(code)
        );
        assertTrue(ex.getMessage().contains("chưa được kích hoạt"));
    }

    @Test
    @DisplayName("applyPromotion ném BadRequestException khi mã đã hết hạn")
    void testApplyPromotion_Expired() {
        String code = "EXPIRED_CODE";
        Promotion promotion = new Promotion();
        promotion.setCode(code);
        promotion.setIsActive(true);
        promotion.setStartDate(LocalDate.now().minusDays(10));
        promotion.setEndDate(LocalDate.now().minusDays(1)); // Đã hết hạn hôm qua

        when(promotionRepository.findByCode(code)).thenReturn(Optional.of(promotion));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                promotionService.applyPromotion(code)
        );
        assertTrue(ex.getMessage().contains("hết hạn"));
    }

    @Test
    @DisplayName("applyPromotion ném BadRequestException khi vượt quá lượt sử dụng")
    void testApplyPromotion_UsageLimitReached() {
        String code = "LIMIT_REACHED";
        Promotion promotion = new Promotion();
        promotion.setCode(code);
        promotion.setIsActive(true);
        promotion.setStartDate(LocalDate.now().minusDays(1));
        promotion.setEndDate(LocalDate.now().plusDays(5));
        promotion.setUsageLimit(10);
        promotion.setUsedCount(10);

        when(promotionRepository.findByCode(code)).thenReturn(Optional.of(promotion));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                promotionService.applyPromotion(code)
        );
        assertTrue(ex.getMessage().contains("hết lượt sử dụng"));
    }

    @Test
    @DisplayName("applyPromotion thành công khi mã hợp lệ")
    void testApplyPromotion_Success() {
        String code = "PROMO50";
        Promotion promotion = new Promotion();
        promotion.setId(1L);
        promotion.setCode(code);
        promotion.setIsActive(true);
        promotion.setStartDate(LocalDate.now().minusDays(1));
        promotion.setEndDate(LocalDate.now().plusDays(5));
        promotion.setUsageLimit(100);
        promotion.setUsedCount(5);

        PromotionDTO dto = new PromotionDTO();
        dto.setCode(code);

        when(promotionRepository.findByCode(code)).thenReturn(Optional.of(promotion));
        when(promotionRepository.incrementUsedCount(1L)).thenReturn(1);
        when(promotionMapper.toDTO(promotion)).thenReturn(dto);

        PromotionDTO result = promotionService.applyPromotion(code);

        assertNotNull(result);
        assertEquals(code, result.getCode());
        assertEquals(6, promotion.getUsedCount());
    }
}
