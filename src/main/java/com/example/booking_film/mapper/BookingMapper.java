package com.example.booking_film.mapper;

import com.example.booking_film.dto.response.BookingResponse;
import com.example.booking_film.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "bookingId", source = "id")
    BookingResponse toDTO(Booking booking);
}
