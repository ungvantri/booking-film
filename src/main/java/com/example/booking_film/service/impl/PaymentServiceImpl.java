package com.example.booking_film.service.impl;

import com.example.booking_film.customexception.BadRequestException;
import com.example.booking_film.customexception.ForbiddenException;
import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.dto.request.PaymentRequest;
import com.example.booking_film.dto.response.PaymentResponse;
import com.example.booking_film.entity.*;
import com.example.booking_film.enums.BookingStatus;
import com.example.booking_film.enums.PaymentStatus;
import com.example.booking_film.enums.Role;
import com.example.booking_film.enums.SeatStatus;
import com.example.booking_film.repository.BookingRepository;
import com.example.booking_film.repository.PaymentRepository;
import com.example.booking_film.repository.SeatShowtimeRepository;
import com.example.booking_film.repository.UserRepository;
import com.example.booking_film.service.PaymentService;
import com.example.booking_film.util.HmacUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
public class PaymentServiceImpl implements PaymentService {
    @Value("${vnpay.version}")
    private String vnp_Version;

    @Value("${vnpay.command}")
    private String vnp_Command;

    @Value("${vnpay.tmn-code}")
    private String vnp_TmnCode;

    @Value("${vnpay.locale}")
    private String vnp_Locale;

    @Value("${vnpay.currcode}")
    private String vnp_CurrCode;

    @Value("${vnpay.return-url}")
    private String vnp_ReturnUrl;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.pay-url}")
    private String payUrl;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SeatShowtimeRepository seatShowtimeRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public PaymentResponse createPayment(String email, PaymentRequest request, String ipAddress) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        Booking booking = bookingRepository.findById(request.getBookingId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thanh toán có id:" + request.getBookingId()));
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalAmount())
                .method(request.getMethod())
                .status(PaymentStatus.PENDING)
                .build();
        booking.setPayment(payment);
        bookingRepository.save(booking);

        boolean isOwner = booking.getUser().getId().equals(user.getId());
        boolean isStaffOrAdmin = user.getRole() == Role.ADMIN || user.getRole() == Role.STAFF;
        if (!isOwner && !isStaffOrAdmin) {
            throw new ForbiddenException("Không có quyền thanh toán booking này");
        }
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", vnp_Version);
        params.put("vnp_Command", vnp_Command);
        params.put("vnp_TmnCode", vnp_TmnCode);
        params.put("vnp_Amount", String.valueOf(booking.getTotalAmount().multiply(BigDecimal.valueOf(100))));
        params.put("vnp_CurrCode", vnp_CurrCode);
        params.put("vnp_TxnRef", String.valueOf(booking.getId()));
        params.put("vnp_OrderInfo", "Thanh toán đơn hàng có id: " + booking.getId());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", vnp_Locale);
        params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        params.put("vnp_IpAddr", ipAddress);

        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        params.put("vnp_CreateDate", createDate);

        LocalDateTime expireTime = booking.getBookingTime().plusMinutes(15);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String expireDate = expireTime.format(formatter);
        params.put("vnp_ExpireDate", expireDate);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String rawValue = entry.getValue();
            String encodedValue = URLEncoder.encode(rawValue, StandardCharsets.US_ASCII);
            hashData.append(key).append('=').append(rawValue).append('&');
            query.append(key).append('=').append(encodedValue).append('&');
        }
        hashData.deleteCharAt(hashData.length() - 1);
        query.deleteCharAt(query.length() - 1);

        String secureHash = HmacUtil.hmacSHA512(hashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        PaymentResponse paymentResponse = PaymentResponse.builder()
                .status(PaymentStatus.PENDING)
                .redirectUrl(payUrl + "?" + query)
                .build();
        return paymentResponse;
    }

    @Override
    public PaymentResponse getPaymentById(String email, Long id) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung"));
        if (!(user.getRole() == Role.ADMIN) && !(user.getRole() == Role.STAFF)) {
            throw new ForbiddenException("Không có quyền truy cập");
        }
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Khong tim thay khoan thanh toan"));
        return PaymentResponse.builder()
                .status(payment.getStatus())
                .transactionCode(payment.getTransactionCode())
                .build();
    }

    @Override
    public void vnpayCallback(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String rawValue = entry.getValue();
            hashData.append(key).append('=').append(rawValue).append('&');
        }
        hashData.deleteCharAt(hashData.length() - 1);
        String calculatedHash = HmacUtil.hmacSHA512(hashSecret, hashData.toString());
        boolean check = calculatedHash.equalsIgnoreCase(vnp_SecureHash);
        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");
        if (!check) {
            throw new BadRequestException("Chữ ký không hợp lệ");
        }
        if (!("00".equals(responseCode)) || !("00".equals(transactionStatus))) {
            throw new BadRequestException("Thanh toán thất bại!");
        }
        Booking booking_param = bookingRepository.findById(Long.valueOf(params.get("vnp_TxnRef"))).orElseThrow(() -> new ResourceNotFoundException("Không tồn tại thông tin đặt hàng"));
        Payment payment = booking_param.getPayment();
        if (payment.getStatus() == PaymentStatus.SUCCESS || booking_param.getStatus() == BookingStatus.PAID) {
            return;
        }
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionCode(params.get("vnp_TransactionNo"));
        payment.setPaidAt(LocalDateTime.now());
        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.PAID);
        List<Long> idBookingSeats = booking.getBookingSeats().stream().map(item -> item.getSeat().getId()).toList();
        for (Long seatId : idBookingSeats) {
            SeatShowtime seatShowtime = seatShowtimeRepository
                    .findByShowtimeIdAndSeatId(booking.getShowtime().getId(), seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin ghế của xuất chiếu"));
            seatShowtime.setStatus(SeatStatus.BOOKED);
            seatShowtimeRepository.save(seatShowtime);
        }
        redisTemplate.delete("booking_timeout:" + booking.getBookingCode());
        bookingRepository.save(booking);
        paymentRepository.save(payment);
    }

    @Override
    public void refundPayment(Long id, String email, String ipAddress) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        if (!(user.getRole() == Role.ADMIN)) {
            throw new ForbiddenException("Không có quyền truy cập");
        }
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thanh toán có id:" + id));
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new BadRequestException("Khoản thanh toán này đã được hoàn trả từ trước!");
        }
        String requestId = UUID.randomUUID().toString();
        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String transactionDate = payment.getPaidAt().format(formatter);
        BigDecimal amount = payment.getAmount().multiply(BigDecimal.valueOf(100));
        String transactionNo = "";
        String hashData = String.join("|",
                requestId,
                vnp_Version,
                "refund",
                vnp_TmnCode,
                "02",
                String.valueOf(payment.getBooking().getId()),
                String.valueOf(amount),
                transactionNo,
                transactionDate,
                "ADMIN",
                createDate,
                ipAddress,
                "Hoan tien"
        );
        String secureHash = HmacUtil.hmacSHA512(hashSecret, hashData);
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("vnp_RequestId", requestId);
        requestBody.put("vnp_Version", vnp_Version);
        requestBody.put("vnp_Command", "refund");
        requestBody.put("vnp_TmnCode", vnp_TmnCode);
        requestBody.put("vnp_TransactionType", "02");
        requestBody.put("vnp_TxnRef", String.valueOf(payment.getBooking().getId()));
        requestBody.put("vnp_Amount", String.valueOf(amount));
        requestBody.put("vnp_OrderInfo", "Hoan tien");
        requestBody.put("vnp_TransactionDate", transactionDate);
        requestBody.put("vnp_CreateBy", "ADMIN");
        requestBody.put("vnp_CreateDate", createDate);
        requestBody.put("vnp_IpAddr", ipAddress);
        requestBody.put("vnp_SecureHash", secureHash);

        RestClient restClient = RestClient.create();

        Map<String, String> response = restClient.post()
                .uri("https://sandbox.vnpayment.vn/merchant_webapi/api/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, String>>() {
                });
    }
}
