package vn.edu.iuh.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.dto.*;
import vn.edu.iuh.exceptions.InvalidRequestException;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.AuthService;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authentication", description = "Xác thực người dùng")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "Đăng ký tài khoản",
            description = """
                    Cập nhật mật khẩu và các thông tin cơ bản sau khi số điện thoại đã được xác thực
                    """,
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            }
    )
    @PostMapping("/register")
    public String register(@RequestBody @Valid RegisterRequestDTO registerRequestDTO, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return authService.register(registerRequestDTO, userPrincipal);
    }

    @Operation(
            summary = "Đăng nhập",
            description = """
                    Đăng nhập và trả về access (30 phút cho môi trường thật và 1 giờ cho môi trường dev) và refresh token (14 ngày)
                    
                    Danh sách tài khoản:
                    - 0354927402 - Nguyễn Minh Quân
                    - 0342036135 - Bành Xuân Mụi
                    - 0961613087 - Trần Khánh Linh
                    - 0929635572 - Hà Huy Hùng
                    - 0939730322 - Phạm Hà Gia Huy
                    - 0354062270 - Trần Quang Khải
                    """
    )
    @PostMapping("/login")
    public TokenResponseDTO login(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {
        return authService.login(loginRequestDTO);
    }

    @Operation(
            summary = "Lấy access token mới", description = """
            Lấy access token mới bằng refresh token
            """
    )
    @PostMapping("/refresh-token")
    public TokenResponseDTO getAccessToken(@RequestBody @Valid RefreshTokenDTO refreshTokenDTO) {
        return authService.getAccessToken(refreshTokenDTO.getToken());
    }

    @Operation(
            summary = "Đăng xuất trên tất cả thiết bị khác", description = """
            Xóa toàn bị refresh token đang ACTIVE trừ token của thiết bị hiện tại
             """
    )
    @PostMapping("/logout/all")
    public String logoutAll(@RequestBody @Valid RefreshTokenDTO refreshTokenDTO) {
        return authService.logoutAll(refreshTokenDTO.getToken());
    }

    @Operation(
            summary = "Đăng xuất khỏi thiết bị hiện tại", description = """
            Xóa refresh token của thiết bị hiện tại
             """
    )
    @PostMapping("/logout")
    public String logout(@RequestBody @Valid RefreshTokenDTO refreshTokenDTO) {
        return authService.logout(refreshTokenDTO.getToken());
    }

    @Operation(
            summary = "Gửi yêu cầu quên mật khẩu", description = """
            Gửi yêu cầu quên mật khẩu và nhận OTP
             """
    )
    @PostMapping("/password/forgot")
    public String forgotPassword(@RequestBody @Valid PhoneNumberDTO phoneNumberDTO) {
        return authService.forgotPassword(phoneNumberDTO.getPhone());
    }

    @Operation(
            summary = "Xác thực OTP cho quá trình lấy lại mật khẩu",
            description = """
                    Xác thực OTP để lấy lại mật khẩu. Nếu OTP hợp lệ hệ thống trả về 1 Reset Token có thời hạn 5 phút. Dùng Reset token để cập nhật lại mật khẩu tại /v1/auth/password/reset
                    """
    )
    @PostMapping("/password/reset/validate")
    public ResetTokenDTO validateResetPasswordOTP(@RequestBody @Valid OTPRequestDTO otpRequestDTO) {
        return authService.validateResetPassword(otpRequestDTO.getPhone(), otpRequestDTO.getOtp());
    }

    @Operation(
            summary = "Cập nhật mật khẩu sau khi xác thực OTP",
            description = """
            Cập nhật mật khẩu mới sau khi xác thực OTP
             """
    )
    @PostMapping("/password/reset")
    public String resetPassword(@RequestBody @Valid ResetPasswordRequestDTO resetPasswordRequestDTO) {
        return authService.resetPassword(resetPasswordRequestDTO.getToken(), resetPasswordRequestDTO.getPassword());
    }
    @Operation(
            summary = "Cập nhật mật khẩu",
            description = """
            Cập nhật mật khẩu mới bằng mật khẩu hiện tại
            
             <strong>Bad Request: </strong>
             - Mật khẩu mới trùng với mật khẩu hiện tại
             - Mật khẩu hiện tại không chính xác
             """
    )
    @PostMapping("/password/change")
    public String changePassword(@RequestBody @Valid ChangePasswordRequestDTO changePasswordRequestDTO, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (changePasswordRequestDTO.getOldPassword().equals(changePasswordRequestDTO.getNewPassword())) {
            throw new InvalidRequestException("Mật khẩu mới không được trùng với mật khẩu hiện tại");
        }
        return authService.changePassword(changePasswordRequestDTO, userPrincipal);
    }
}
