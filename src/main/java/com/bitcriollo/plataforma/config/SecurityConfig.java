package com.bitcriollo.plataforma.config;

import com.bitcriollo.plataforma.security.AuthEntryPointJwt;
import com.bitcriollo.plataforma.security.CsrfAccessDeniedHandler;
import com.bitcriollo.plataforma.security.RateLimitFilter;
import com.bitcriollo.plataforma.security.JwtAuthFilter;
import com.bitcriollo.plataforma.security.UsuarioDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import java.util.Arrays;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final UsuarioDetailsServiceImpl usuarioDetailsService;
    private final JwtAuthFilter jwtAuthFilter;
    private final AuthEntryPointJwt authEntryPointJwt;
    private final CsrfAccessDeniedHandler csrfAccessDeniedHandler;
    private final RateLimitFilter rateLimitFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.auth.refresh-cookie.enabled:false}")
    private boolean refreshCookieEnabled;

    public SecurityConfig(UsuarioDetailsServiceImpl usuarioDetailsService, JwtAuthFilter jwtAuthFilter,
            AuthEntryPointJwt authEntryPointJwt, CsrfAccessDeniedHandler csrfAccessDeniedHandler,
            RateLimitFilter rateLimitFilter) {
        this.usuarioDetailsService = usuarioDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
        this.authEntryPointJwt = authEntryPointJwt;
        this.csrfAccessDeniedHandler = csrfAccessDeniedHandler;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Las solicitudes del frontend se permiten únicamente desde los orígenes configurados.
        List<String> origins = Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(s -> !s.isBlank())
                .toList();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN", "X-Correlation-Id"));
        // El comodin es util mientras no exista un frontend desplegado, pero el
        // navegador no permite combinarlo con credenciales o cookies.
        config.setAllowCredentials(!origins.contains("*"));
        config.setExposedHeaders(List.of("X-Correlation-Id"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> {
                    csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
                    if (!refreshCookieEnabled) {
                        // El modo JSON no autentica con cookies; se conserva el contrato existente
                        // sin exponer los endpoints a CSRF basado en cookies.
                        csrf.ignoringRequestMatchers("/api/**");
                    }
                })
                // La API utiliza JWT, por lo que no mantiene sesiones HTTP en el servidor.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Sin token o con un token invalido se responde 401; los fallos de autorizacion siguen en 403.
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authEntryPointJwt)
                        .accessDeniedHandler(csrfAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // Salud y autenticación son públicos para permitir verificar el servicio e iniciar sesión.
                        .requestMatchers("/api/health", "/api/auth/**").permitAll()
                        // El reenvio interno de Spring Boot a /error (por ejemplo ante un JSON invalido) vuelve a
                        // pasar por este filtro; sin este permiso, JwtAuthFilter no corre en ese reenvio (no
                        // filtra despachos de tipo ERROR) y el error original se reemplaza por un 401.
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/cursos", "/api/cursos/*", "/api/usuarios/*/foto")
                        .permitAll()
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                // El filtro JWT valida el token antes de que Spring procese la autenticación estándar.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
                // Rate limiting is deliberately placed before authentication to also protect login/refresh.
        http.addFilterBefore(rateLimitFilter, JwtAuthFilter.class);

        return http.build();
    }
}