package com.example.booking_film.mapper;

import com.example.booking_film.dto.response.PromotionDTO;
import com.example.booking_film.entity.Promotion;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PromotionMapper {
    List<PromotionDTO> toDTO(List<Promotion> promotions);

    PromotionDTO toDTO(Promotion promotion);

    Promotion toEntity(PromotionDTO promotionDTO);
}
