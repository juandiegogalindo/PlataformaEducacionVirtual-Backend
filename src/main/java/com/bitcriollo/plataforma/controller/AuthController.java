package com.bitcriollo.plataforma.controller;

import com.bitcriollo.plataforma.dto.AuthResponse;
import com.bitcriollo.plataforma.dto.LoginRequest;
import com.bitcriollo.plataforma.dto.RefreshTokenRequest;
import com.bitcriollo.plataforma.dto.RegistroRequest;
import com.bitcriollo.plataforma.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.web.csrf.CsrfToken;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${app.auth.refresh-cookie.enabled:false}")
    private boolean refreshCookieEnabled;

    @Value("${app.auth.refresh-cookie.secure:false}")
    private boolean refreshCookieSecure;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registro(@Valid @RequestBody RegistroRequest request,
            HttpServletResponse httpResponse) {
        AuthResponse response = authService.registrarEstudiante(request);
        agregarCookie(httpResponse, response.getRefreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(respuestaParaCliente(response));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        AuthResponse response = authService.login(request, httpRequest.getRemoteAddr());
        agregarCookie(httpResponse, response.getRefreshToken());
        return ResponseEntity.ok(respuestaParaCliente(response));
    }

    @GetMapping("/csrf")
    public ResponseEntity<String> csrf(CsrfToken token) {
        return ResponseEntity.ok(token.getToken());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest, HttpServletResponse response) {
        RefreshTokenRequest effective = request != null ? request : new RefreshTokenRequest();
        if ((effective.getRefreshToken() == null || effective.getRefreshToken().isBlank())
                && refreshCookieEnabled && httpRequest.getCookies() != null) {
            for (var cookie : httpRequest.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    effective.setRefreshToken(cookie.getValue());
                    break;
                }
            }
        }
        AuthResponse authResponse = authService.refrescarToken(effective);
        agregarCookie(response, authResponse.getRefreshToken());
        return ResponseEntity.ok(respuestaParaCliente(authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest, HttpServletResponse response) {
        RefreshTokenRequest effective = request != null ? request : new RefreshTokenRequest();
        if ((effective.getRefreshToken() == null || effective.getRefreshToken().isBlank())
                && refreshCookieEnabled && httpRequest.getCookies() != null) {
            for (var cookie : httpRequest.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    effective.setRefreshToken(cookie.getValue());
                    break;
                }
            }
        }
        authService.logout(effective);
        if (refreshCookieEnabled) {
            response.addHeader("Set-Cookie", ResponseCookie.from("refresh_token", "")
                    .httpOnly(true).secure(refreshCookieSecure).sameSite("Lax")
                    .path("/api/auth").maxAge(0).build().toString());
        }
        return ResponseEntity.noContent().build();
    }

    private void agregarCookie(HttpServletResponse response, String token) {
        if (refreshCookieEnabled) {
            response.addHeader("Set-Cookie", ResponseCookie.from("refresh_token", token)
                    .httpOnly(true).secure(refreshCookieSecure).sameSite("Lax")
                    .path("/api/auth").maxAge(java.time.Duration.ofDays(7)).build().toString());
        }
    }

    private AuthResponse respuestaParaCliente(AuthResponse response) {
        return refreshCookieEnabled
                ? new AuthResponse(response.getAccessToken(), null, response.getTipo(),
                        response.getCorreo(), response.getRol())
                : response;
    }
}