package com.example.backendplantshop.dto.request.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDtoRequest {
    @NotBlank(message = "username không được bỏ trống")
    private String username;
    @NotBlank(message = "email không được bỏ trống")
    private String email;
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 20, message = "Mật khẩu phải tối thiểu 8 kí tự!")
    private String password;
    private String role; // Optional: chỉ admin mới được set role khi đăng ký
    @NotBlank(message = "Mã OTP không được để trống")
    private String otpCode; // Mã OTP để xác thực email

}
