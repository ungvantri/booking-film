package com.example.booking_film.dto.response;

import com.example.booking_film.enums.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class RoomDTO {
    private Long id;

    @NotBlank(message = "Tên phòng không được để trống")
    private String name;

    @NotNull(message = "Loại phòng không được để trống")
    private RoomType type;

    @Min(value = 0, message = "Số lượng ghế phải lớn hơn hoặc bằng 0")
    private Integer totalSeats;

    private Long cinemaId;
}
