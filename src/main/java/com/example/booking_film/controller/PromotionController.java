package com.example.booking_film.controller;

import com.example.booking_film.dto.response.ApiResponse;
import com.example.booking_film.dto.response.PromotionDTO;
import com.example.booking_film.service.PromotionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {
    @Autowired
    private PromotionService promotionService;

    @GetMapping("/active")
    public ApiResponse<List<PromotionDTO>> getActivePromotions() {
        List<PromotionDTO> promotionDTOS = promotionService.getActivePromotions();
        return ApiResponse.<List<PromotionDTO>>builder()
                .success(true)
                .message("Lấy danh sách các khuyến mãi có hiệu lực thành công")
                .data(promotionDTOS)
                .build();
    }

    @PostMapping("/apply")
    public ApiResponse<PromotionDTO> applyPromotion(@RequestParam String code) {
        PromotionDTO promotionDTO = promotionService.applyPromotion(code);
        return ApiResponse.<PromotionDTO>builder()
                .success(true)
                .message("Khuyến mãi hợp lệ")
                .data(promotionDTO)
                .build();
    }

    @GetMapping
    public ApiResponse<List<PromotionDTO>> getAllPromotions() {
        List<PromotionDTO> promotionDTOS = promotionService.getAllPromotions();
        return ApiResponse.<List<PromotionDTO>>builder()
                .success(true)
                .message("Lấy danh sách các khuyến mãi thành công")
                .data(promotionDTOS)
                .build();
    }

    @PostMapping
    public ApiResponse<PromotionDTO> createPromotion(@Valid @RequestBody PromotionDTO request) {
        PromotionDTO promotionDTO = promotionService.createPromotion(request);
        return ApiResponse.<PromotionDTO>builder()
                .success(true)
                .message("Tạo khuyến mãi thành công")
                .data(promotionDTO)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<PromotionDTO> updatePromotion(@PathVariable Long id, @Valid @RequestBody PromotionDTO request) {
        PromotionDTO promotionDTO = promotionService.updatePromotion(id, request);
        return ApiResponse.<PromotionDTO>builder()
                .success(true)
                .message("Cập nhật thông tin khuyến mãi thành công")
                .data(promotionDTO)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa khuyến mãi thành công")
                .build();
    }
}
