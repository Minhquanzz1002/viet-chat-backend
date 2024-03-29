package vn.edu.iuh.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    @NotNull(message = "Họ là bắt buộc")
    @NotBlank(message = "Họ không được rỗng")
    private String firstName;
    @NotNull(message = "Tên là bắt buộc")
    @NotBlank(message = "Tên không được rỗng")
    private String lastName;
    @NotNull(message = "Giới tính là bắt buộc")
    private boolean gender;
    @NotNull(message = "Ngày sinh là bắt buộc")
    @Past(message = "Ngày sinh phải trước ngày hiện tại")
    private LocalDate birthday;
    @NotNull(message = "Mật khẩu là bắt buộc")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=])(?=\\S+$).{8,32}$", message = "Mật khẩu từ 8 - 32 ký tự gồm tối thiểu 1 chữ cái viết hoa, 1 chữ cái viết thường, 1 chữ số và 1 ký tự đặc biệt")
    private String password;
}
