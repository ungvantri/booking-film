package com.example.booking_film.service.impl;

import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.entity.Booking;
import com.example.booking_film.repository.BookingRepository;
import com.example.booking_film.service.EmailService;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private BookingRepository bookingRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async
    @Retry(name = "emailRetry", fallbackMethod = "sendBookingSuccessEmailFallback")
    public void sendBookingSuccessEmail(Long bookingId) {
        try {
            Booking booking = bookingRepository.findByIdWithDetails(bookingId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin đặt vé"));
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            String toEmail = booking.getUser().getEmail();
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Xác Nhận Đặt Vé Thành Công - Mã Đơn: " + booking.getBookingCode());
            // Định dạng ngày giờ và tiền tệ
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
            String showtimeStr = booking.getShowtime().getStartTime().format(dateFormatter);

            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String amountStr = currencyFormatter.format(booking.getTotalAmount());
            // Danh sách mã ghế (ví dụ: A1, A2)
            String seatCodes = booking.getBookingSeats().stream()
                    .map(bs -> bs.getSeat().getSeatCode())
                    .collect(Collectors.joining(", "));
            String movieTitle = booking.getShowtime().getMovie().getTitle();
            String cinemaName = booking.getShowtime().getRoom().getCinema().getName();
            String roomName = booking.getShowtime().getRoom().getName();
            String content = """
                <div style="font-family: Arial, sans-serif; padding: 25px; color: #333; max-width: 600px; margin: auto; border: 1px solid #e0e0e0; border-radius: 8px;">
                    <div style="text-align: center; border-bottom: 2px solid #e50914; padding-bottom: 15px;">
                        <h2 style="color: #e50914; margin: 0;">THANH TOÁN THÀNH CÔNG!</h2>
                        <p style="color: #666; margin: 5px 0 0 0;">Cảm ơn bạn đã đặt vé tại hệ thống của chúng tôi</p>
                    </div>
                    <div style="margin: 20px 0;">
                        <p><strong>Mã vé (Booking Code):</strong> <span style="font-size: 18px; color: #e50914; font-weight: bold;">%s</span></p>
                        <table style="width: 100%%; border-collapse: collapse; margin-top: 15px;">
                            <tr style="border-bottom: 1px solid #eee;">
                                <td style="padding: 8px 0; color: #666;">Phim:</td>
                                <td style="padding: 8px 0; font-weight: bold; text-align: right;">%s</td>
                            </tr>
                            <tr style="border-bottom: 1px solid #eee;">
                                <td style="padding: 8px 0; color: #666;">Rạp chiếu:</td>
                                <td style="padding: 8px 0; text-align: right;">%s</td>
                            </tr>
                            <tr style="border-bottom: 1px solid #eee;">
                                <td style="padding: 8px 0; color: #666;">Phòng chiếu:</td>
                                <td style="padding: 8px 0; text-align: right;">%s</td>
                            </tr>
                            <tr style="border-bottom: 1px solid #eee;">
                                <td style="padding: 8px 0; color: #666;">Suất chiếu:</td>
                                <td style="padding: 8px 0; font-weight: bold; text-align: right;">%s</td>
                            </tr>
                            <tr style="border-bottom: 1px solid #eee;">
                                <td style="padding: 8px 0; color: #666;">Ghế ngồi:</td>
                                <td style="padding: 8px 0; font-weight: bold; color: #e50914; text-align: right;">%s</td>
                            </tr>
                            <tr style="border-top: 2px solid #333;">
                                <td style="padding: 12px 0; font-size: 16px; font-weight: bold;">Tổng tiền đã thanh toán:</td>
                                <td style="padding: 12px 0; font-size: 18px; font-weight: bold; color: #e50914; text-align: right;">%s</td>
                            </tr>
                        </table>
                    </div>
                    <p style="font-size: 13px; color: #666; background: #fdf5e6; padding: 10px; border-radius: 4px;">
                        📌 <b>Lưu ý:</b> Vui lòng xuất trình mã vé tại quầy trước giờ chiếu 15 phút để in vé vào phòng.
                    </p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;" />
                    <p style="font-size: 12px; color: #888; text-align: center;">Chúc bạn có những giây phút xem phim vui vẻ!</p>
                </div>
            """.formatted(booking.getBookingCode(), movieTitle, cinemaName, roomName, showtimeStr, seatCodes, amountStr);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi gửi mail: " + e.getMessage(), e);
        }
    }

    //Fallback phải nằm cùng class với method gốc
    //Fallback phải có cùng kiểu trả về với method gốc
    //Fallback có đúng các tham số của method gốc, rồi thêm một tham số Exception ở cuối
    public void sendBookingSuccessEmailFallback(Long bookingId, Throwable ex) {
        System.out.print("HẾT LẦN THỬ LẠI! Gửi email cho bookingId: " + bookingId + " thất bại hoàn toàn. Nguyên nhân: " + ex.getMessage());
    }
}
