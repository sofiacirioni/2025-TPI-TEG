package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.Dtos.Auth.AuthResponseDto;
import ar.edu.utn.frc.tup.piii.Dtos.Auth.LoginRequestDto;
import ar.edu.utn.frc.tup.piii.Dtos.Auth.RegisterRequestDto;
import ar.edu.utn.frc.tup.piii.Entities.UsuarioEntity;
import ar.edu.utn.frc.tup.piii.Repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.piii.Security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpiration;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

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

    public AuthResponseDto refresh(HttpServletRequest request) {
        String refreshToken = extractRefreshCookie(request);

        if (refreshToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token ausente");
        }

        if (!jwtService.isTokenValid(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido");
        }

        String correo = jwtService.extractUsername(refreshToken);
        UsuarioEntity user = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        String newAccessToken = jwtService.generateAccessToken(correo);
        return new AuthResponseDto(newAccessToken, user.getIdUsuario(), user.getUsuario(),
                user.getCorreo(), user.getImagen());
    }

    private String extractRefreshCookie(HttpServletRequest req) {
        if (req.getCookies() == null) return null;
        for (Cookie cookie : req.getCookies()) {
            if ("refresh_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void logout(HttpServletResponse response) {
        clearRefreshTokenCookie(response);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie
                .from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/v1/auth/refresh")
                .maxAge(refreshExpiration / 1000)
                .sameSite(cookieSecure ? "Strict" : "Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie
                .from("refresh_token", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/v1/auth/refresh")
                .maxAge(0)
                .sameSite(cookieSecure ? "Strict" : "Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
