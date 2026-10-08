package com.example.booking_film.service;

import com.example.booking_film.dto.response.PromotionDTO;

import java.util.List;

public interface PromotionService {
    List<PromotionDTO> getActivePromotions();

    PromotionDTO validatePromotion(String code);

    List<PromotionDTO> getAllPromotions();

    PromotionDTO createPromotion(PromotionDTO request);

    PromotionDTO updatePromotion(Long id, PromotionDTO request);

    void deletePromotion(Long id);
}
