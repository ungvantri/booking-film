package com.example.booking_film.service.impl;

import com.example.booking_film.customexception.ConflictException;
import com.example.booking_film.customexception.ForbiddenException;
import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.dto.request.BookingRequest;
import com.example.booking_film.dto.response.*;
import com.example.booking_film.entity.*;
import com.example.booking_film.enums.*;
import com.example.booking_film.mapper.BookingMapper;
import com.example.booking_film.mapper.MovieMapper;
import com.example.booking_film.mapper.SeatMapper;
import com.example.booking_film.mapper.ShowtimeMapper;
import com.example.booking_film.repository.*;
import com.example.booking_film.service.BookingService;
import com.example.booking_film.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class BookingServiceImpl implements BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private SeatShowtimeRepository seatShowtimeRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private ShowtimeMapper showtimeMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public BookingResponse createBooking(BookingRequest request, String email) {
        // Lấy thông tin người dùng
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        // Kiểm tra tính hợp lệ của voucher
        PromotionDTO promotionDTO = request.getPromotionCode() != null ? promotionService.validatePromotion(request.getPromotionCode()) : null;

        // Kiểm tra lịch chiếu có phù hợp không
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy suất chiếu với id: " + request.getShowtimeId()));
        if (showtime.getStatus() != ShowtimeStatus.SCHEDULED) {
            throw new ConflictException("Suất chiếu này không còn nhận đặt vé (đã huỷ hoặc đã diễn ra)");
        }
        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ConflictException("Suất chiếu đã bắt đầu hoặc đã kết thúc, không thể đặt vé");
        }

        // Khóa ghế
        List<SeatShowtime> lockedSeats = seatShowtimeRepository.findSeatsForBookingWithLock(request.getShowtimeId(), request.getSeatIds());

        // Kiểm tra tính hợp lệ của ghế được đặt
        if (lockedSeats.size() != request.getSeatIds().size()) {
            throw new ResourceNotFoundException("Một số ghế không tồn tại trong suất chiếu này");
        }
        for (SeatShowtime seatShowtime : lockedSeats) {
            if (!seatShowtime.getStatus().equals(SeatStatus.AVAILABLE)) {
                throw new ConflictException("Ghế có id: " + seatShowtime.getSeat().getId() + "  đã có người đặt hoặc đang giữ!");
            }
            Object user_id = redisTemplate.opsForValue().get("seat_hold:" + request.getShowtimeId() + ":" + seatShowtime.getSeat().getId());
            if(user_id != null) {
                throw new ConflictException("Ghế có id: " + seatShowtime.getSeat().getId() + " đang bị tạm thời bị giữ bởi một khách hàng khác");
            }
        }

        BigDecimal price = showtime.getSeatShowtimes().stream()
                .filter(item -> request.getSeatIds().contains(item.getSeat().getId()))
                .map(item -> item.getPrice())
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
        if (promotionDTO != null) {
            if (promotionDTO.getDiscountType().equals(DiscountType.PERCENT)) {
                BigDecimal discountValue = promotionDTO.getDiscountValue();
                BigDecimal remainingPercent = BigDecimal.valueOf(100).subtract(discountValue);
                price = price.multiply(remainingPercent).divide(BigDecimal.valueOf(100));
            } else {
                price = price.subtract(promotionDTO.getDiscountValue());
            }
        }
        Booking booking = Booking.builder()
                .bookingCode(UUID.randomUUID().toString())
                .user(user)
                .showtime(showtime)
                .bookingTime(LocalDateTime.now())
                .totalAmount(price)
                .status(BookingStatus.PENDING)
                .build();

        List<BookingSeat> bookingSeats = request.getSeatIds().stream()
                .map(item -> BookingSeat.builder()
                        .booking(booking)
                        .seat(seatRepository.findById(item).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ghế có id: " + item)))
                        .price(seatShowtimeRepository.findByShowtimeIdAndSeatId(request.getShowtimeId(), item).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin showtime hợp lệ")).getPrice())
                        .build())
                .toList();

        booking.setBookingSeats(bookingSeats);

        // Lưu các ghế tạm thời đang được hold trong 15 phút
        for(Long id : request.getSeatIds()) {
            redisTemplate.opsForValue().set("seat_hold:" + request.getShowtimeId() + ":" + id + ":" + booking.getBookingCode(), user.getId(), 900, TimeUnit.SECONDS);
        }

        Booking saved = bookingRepository.save(booking);

        BookingResponse bookingResponse = bookingMapper.toDTO(saved);
        bookingResponse.setShowtime(showtimeMapper.toDto(showtime));
        List<SeatDTO> seatDTOS = seatMapper.toDTO(seatRepository.findAllById(request.getSeatIds()));
        bookingResponse.setSeats(seatDTOS);
        MovieDTO movieDTO = movieMapper.toDto(saved.getShowtime().getMovie());
        bookingResponse.setMovie(movieDTO);
        return bookingResponse;
    }

    @Override
    public PageResponse<BookingResponse> getMyBookings(String email, int page, int size) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> pages = bookingRepository.findByUserId(user.getId(), pageable);
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (Booking booking : pages.getContent()) {
            BookingResponse bookingResponse = bookingMapper.toDTO(booking);
            bookingResponse.setShowtime(showtimeMapper.toDto(booking.getShowtime()));
            bookingResponse.setMovie(movieMapper.toDto(booking.getShowtime().getMovie()));
            bookingResponse.setSeats(seatMapper.toDTO(booking.getBookingSeats().stream().map(item -> item.getSeat()).toList()));
            bookingResponses.add(bookingResponse);
        }
        return PageResponse.<BookingResponse>builder()
                .items(bookingResponses)
                .currentPage(pages.getNumber())
                .pageSize(pages.getSize())
                .totalItems(pages.getTotalElements())
                .totalPages(pages.getTotalPages())
                .hasNext(pages.hasNext())
                .hasPrevious(pages.hasPrevious())
                .build();
    }

    @Override
    public BookingResponse getBookingById(String email, Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin đặt phòng có id: " + id));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        BookingResponse bookingResponse = bookingMapper.toDTO(booking);
        bookingResponse.setShowtime(showtimeMapper.toDto(booking.getShowtime()));
        bookingResponse.setMovie(movieMapper.toDto(booking.getShowtime().getMovie()));
        bookingResponse.setSeats(seatMapper.toDTO(booking.getBookingSeats().stream().map(item -> item.getSeat()).toList()));
        if (user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.STAFF)) {
            return bookingResponse;
        }
        if (!bookingRepository.existsByIdAndUserId(id, user.getId())) {
            throw new ForbiddenException("Không có quyền truy cập");
        }
        return bookingResponse;
    }

    @Override
    public BookingResponse getBookingByCode(String email, String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin vé có code là: " + bookingCode));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        BookingResponse bookingResponse = bookingMapper.toDTO(booking);
        bookingResponse.setShowtime(showtimeMapper.toDto(booking.getShowtime()));
        bookingResponse.setMovie(movieMapper.toDto(booking.getShowtime().getMovie()));
        bookingResponse.setSeats(seatMapper.toDTO(booking.getBookingSeats().stream().map(item -> item.getSeat()).toList()));
        if (!user.getRole().equals(Role.ADMIN) && !user.getRole().equals(Role.STAFF)) {
            throw new ForbiddenException("Không có quyền truy cập");
        }
        return bookingResponse;
    }

    @Override
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin đặt phòng có id:" + id));
        List<Long> idSeat = booking.getBookingSeats().stream().map(item -> item.getSeat().getId()).toList();
        Long idShowtime = booking.getShowtime().getId();
        for(Long item : idSeat) {
            SeatShowtime seatShowtime = seatShowtimeRepository.findByShowtimeIdAndSeatId(idShowtime, item).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin đặt phòng"));
            seatShowtime.setStatus(SeatStatus.AVAILABLE);
            redisTemplate.delete("seat_hold:" +idShowtime + ":" + item);
            seatShowtimeRepository.save(seatShowtime);
        }
        booking.getBookingSeats().clear();
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Override
    public PageResponse<BookingResponse> getAllBookings(String email, BookingStatus status, Long userId, LocalDate date, int page, int size) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime start = date != null ? date.atStartOfDay() : null;
        LocalDateTime end = date != null ? date.plusDays(1).atStartOfDay() : null;
        Page<Booking> pages = bookingRepository.findByFilter(status, userId, start, end, pageable);
        if (!user.getRole().equals(Role.ADMIN) && !user.getRole().equals(Role.STAFF)) {
            throw new ForbiddenException("Không có quyền truy cập");
        }
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (Booking booking : pages.getContent()) {
            BookingResponse bookingResponse = bookingMapper.toDTO(booking);
            bookingResponse.setShowtime(showtimeMapper.toDto(booking.getShowtime()));
            bookingResponse.setMovie(movieMapper.toDto(booking.getShowtime().getMovie()));
            bookingResponse.setSeats(seatMapper.toDTO(booking.getBookingSeats().stream().map(item -> item.getSeat()).toList()));
            bookingResponses.add(bookingResponse);
        }
        return PageResponse.<BookingResponse>builder()
                .items(bookingResponses)
                .currentPage(pages.getNumber())
                .pageSize(pages.getSize())
                .totalItems(pages.getTotalElements())
                .totalPages(pages.getTotalPages())
                .hasNext(pages.hasNext())
                .hasPrevious(pages.hasPrevious())
                .build();
    }

}
