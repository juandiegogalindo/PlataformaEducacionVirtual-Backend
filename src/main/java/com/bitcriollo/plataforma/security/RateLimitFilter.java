package com.bitcriollo.plataforma.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 60_000L;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || !debeLimitar(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getRemoteAddr() + ":" + grupo(path);
        Window window = windows.compute(key, (ignored, current) -> {
            long now = Instant.now().toEpochMilli();
            if (current == null || now - current.startedAt >= WINDOW_MILLIS) {
                return new Window(now, 1);
            }
            return new Window(current.startedAt, current.count + 1);
        });
        int limit = limite(grupo(path));
        if (window.count > limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json");
            response.getWriter().write("{\"mensaje\":\"Demasiadas solicitudes\",\"codigo\":\"RATE_LIMIT_EXCEEDED\",\"errores\":[]}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean debeLimitar(String path) {
        return path.equals("/api/auth/login") || path.equals("/api/auth/refresh")
                || path.equals("/api/auth/logout") || path.startsWith("/api/perfil/foto")
                || path.startsWith("/api/admin");
    }

    private String grupo(String path) {
        if (path.contains("/login")) return "login";
        if (path.contains("/refresh")) return "refresh";
        if (path.contains("/logout")) return "logout";
        if (path.startsWith("/api/admin")) return "admin";
        return "upload";
    }

    private int limite(String grupo) {
        return switch (grupo) {
            case "login" -> 10;
            case "refresh", "logout" -> 20;
            case "upload" -> 30;
            default -> 60;
        };
    }

    private record Window(long startedAt, int count) {
    }
}
