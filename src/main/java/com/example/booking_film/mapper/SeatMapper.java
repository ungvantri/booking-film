package com.example.booking_film.mapper;

import com.example.booking_film.dto.response.SeatDTO;
import com.example.booking_film.entity.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SeatMapper {
    @Mapping(target = "seatId", source = "id")
    @Mapping(target = "roomId", expression = "java(seat.getRoom().getId())")
    SeatDTO toDTO (Seat seat);
    List<SeatDTO> toDTO (List<Seat> seats);
}
