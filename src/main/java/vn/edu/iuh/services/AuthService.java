package vn.edu.iuh.services;

import vn.edu.iuh.dto.LoginRequestDTO;
import vn.edu.iuh.dto.TokenResponseDTO;
import vn.edu.iuh.dto.RegisterRequestDTO;
import vn.edu.iuh.security.UserPrincipal;

public interface AuthService {
    TokenResponseDTO login(LoginRequestDTO loginRequestDTO);
    String register(RegisterRequestDTO registerRequestDTO, UserPrincipal userPrincipal);
    TokenResponseDTO getAccessToken(String oldRefreshToken);
    String logout(String token);
    String logoutAll(String token);

}
