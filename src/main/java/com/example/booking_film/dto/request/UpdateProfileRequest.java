package com.example.booking_film.dto.request;


import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProfileRequest {
    private String fullName;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không đúng định dạng")
    private String phoneNumber;

    private String avatarUrl;

    @Past(message = "Ngày sinh phải ở trong quá khứ")
    private LocalDate dateOfBirth;
}
