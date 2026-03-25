package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.Dtos.Auth.AuthResponseDto;
import ar.edu.utn.frc.tup.piii.Dtos.Auth.LoginRequestDto;
import ar.edu.utn.frc.tup.piii.Dtos.Auth.RefreshResponseDto;
import ar.edu.utn.frc.tup.piii.Dtos.Auth.RegisterRequestDto;
import ar.edu.utn.frc.tup.piii.Entities.UsuarioEntity;
import ar.edu.utn.frc.tup.piii.Repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.piii.Security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpiration;

    public AuthResponseDto login(LoginRequestDto request, HttpServletResponse response) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getContrasenia())
        );

        UsuarioEntity user = usuarioRepository.findByCorreo(request.getCorreo()).orElseThrow();

        String accessToken  = jwtService.generateAccessToken(user.getCorreo());
        String refreshToken = jwtService.generateRefreshToken(user.getCorreo());

        setRefreshTokenCookie(response, refreshToken);

        return new AuthResponseDto(accessToken, user.getIdUsuario(), user.getUsuario(),
                user.getCorreo(), user.getImagen());
    }

    public AuthResponseDto register(RegisterRequestDto request, HttpServletResponse response) {
        if (usuarioRepository.findByCorreo(request.getCorreo()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo.");
        }

        UsuarioEntity user = new UsuarioEntity();
        user.setUsuario(request.getUsuario());
        user.setCorreo(request.getCorreo());
        user.setContrasenia(passwordEncoder.encode(request.getContrasenia()));
        user.setImagen(request.getImagen());
        usuarioRepository.save(user);

        String accessToken  = jwtService.generateAccessToken(user.getCorreo());
        String refreshToken = jwtService.generateRefreshToken(user.getCorreo());

        setRefreshTokenCookie(response, refreshToken);

        return new AuthResponseDto(accessToken, user.getIdUsuario(), user.getUsuario(),
                user.getCorreo(), user.getImagen());
    }

    public RefreshResponseDto refresh(HttpServletRequest request) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null || !jwtService.isTokenValid(refreshToken)) {
            throw new IllegalStateException("Refresh token inválido o ausente.");
        }

        String correo = jwtService.extractUsername(refreshToken);
        String newAccessToken = jwtService.generateAccessToken(correo);
        return new RefreshResponseDto(newAccessToken);
    }

    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/v1/auth/refresh");
        response.addCookie(cookie);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true en producción con HTTPS
        cookie.setPath("/api/v1/auth/refresh");
        cookie.setMaxAge((int) (refreshExpiration / 1000));
        response.addCookie(cookie);
    }
}
