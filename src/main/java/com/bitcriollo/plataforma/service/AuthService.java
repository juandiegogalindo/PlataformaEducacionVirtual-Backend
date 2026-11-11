package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.AuthResponse;
import com.bitcriollo.plataforma.dto.LoginRequest;
import com.bitcriollo.plataforma.dto.RefreshTokenRequest;
import com.bitcriollo.plataforma.dto.RegistroRequest;
import com.bitcriollo.plataforma.exception.CredencialesInvalidasException;
import com.bitcriollo.plataforma.model.Estudiante;
import com.bitcriollo.plataforma.model.RefreshToken;
import com.bitcriollo.plataforma.model.Usuario;
import com.bitcriollo.plataforma.repository.EstudianteRepository;
import com.bitcriollo.plataforma.repository.RefreshTokenRepository;
import com.bitcriollo.plataforma.repository.UsuarioRepository;
import com.bitcriollo.plataforma.security.JwtService;
import com.bitcriollo.plataforma.security.UsuarioDetailsImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthAuditService authAuditService;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    public AuthService(UsuarioRepository usuarioRepository,
            EstudianteRepository estudianteRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthAuditService authAuditService) {
        this.usuarioRepository = usuarioRepository;
        this.estudianteRepository = estudianteRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authAuditService = authAuditService;
    }

    @Transactional
    public AuthResponse registrarEstudiante(RegistroRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese correo");
        }

        Estudiante estudiante = new Estudiante();
        estudiante.setNombre(request.getNombre());
        estudiante.setApellido(request.getApellido());
        estudiante.setCorreo(request.getCorreo());
        estudiante.setContrasenaHash(passwordEncoder.encode(request.getContrasena()));
        estudiante.setCodigoEstudiantil(request.getCodigoEstudiantil());
        estudiante.setTipoDocumento(request.getTipoDocumento());
        estudiante.setNumeroDocumento(request.getNumeroDocumento());
        estudiante.setProgramaAcademico(request.getProgramaAcademico());
        estudiante.setSemestre(request.getSemestre());

        estudianteRepository.save(estudiante);

        return generarRespuestaAuth(estudiante);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipOrigen) {
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo()).orElse(null);

        // Se bloquea temporalmente el acceso cuando el usuario supera el límite de intentos fallidos.
        if (usuario != null && usuario.getBloqueadoHasta() != null
                && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            authAuditService.registrarLogAcceso(usuario, request.getCorreo(), false, ipOrigen);
            throw new IllegalStateException("Cuenta bloqueada temporalmente por intentos fallidos");
        }

        if (usuario != null && !usuario.isActivo()) {
            authAuditService.registrarLogAcceso(usuario, request.getCorreo(), false, ipOrigen);
            throw new IllegalStateException("Esta cuenta ha sido desactivada");
        }

        boolean credencialesValidas = usuario != null
                && passwordEncoder.matches(request.getContrasena(), usuario.getContrasenaHash());

        authAuditService.registrarLogAcceso(usuario, request.getCorreo(), credencialesValidas, ipOrigen);
        // Las credenciales inválidas se registran y aumentan el contador de intentos fallidos.
        if (!credencialesValidas) {
            if (usuario != null) {
                authAuditService.registrarIntentoFallido(usuario);
            }
            // Las credenciales invalidas se informan como HTTP 401 (estandar 2.1.4), no como HTTP 400.
            throw new CredencialesInvalidasException("Correo o contrasena incorrectos");
        }
        // Un inicio de sesión exitoso restablece el contador y elimina el bloqueo temporal.
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        return generarRespuestaAuth(usuario);
    }

    @Transactional
    public AuthResponse refrescarToken(RefreshTokenRequest request) {
        String rawToken = validarRefreshRequest(request);
        RefreshToken refreshToken = buscarRefreshToken(rawToken)
                .orElseThrow(() -> new CredencialesInvalidasException("Refresh token invalido"));

        if (refreshToken.isRevocado() || refreshToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            if (refreshToken.isRevocado()) {
                java.util.List<RefreshToken> sessionTokens = refreshToken.getSessionId() == null
                        ? java.util.List.of(refreshToken)
                        : refreshTokenRepository.findBySessionId(refreshToken.getSessionId());
                if (sessionTokens != null) {
                    sessionTokens.forEach(token -> {
                        token.setRevocado(true);
                        token.setRevokedAt(LocalDateTime.now());
                    });
                    refreshTokenRepository.saveAll(sessionTokens);
                }
            }
            throw new CredencialesInvalidasException("Refresh token expirado o revocado");
        }

        Usuario usuario = refreshToken.getUsuario();
        if (!usuario.isActivo() || (usuario.getBloqueadoHasta() != null
                && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now()))) {
            throw new CredencialesInvalidasException("No se puede renovar la sesion");
        }

        refreshToken.setRevocado(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);

        // El refresh token se revoca antes de emitir uno nuevo para evitar su reutilización.
        return generarRespuestaAuth(usuario, refreshToken.getSessionId());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        String rawToken = validarRefreshRequest(request);
        RefreshToken refreshToken = buscarRefreshToken(rawToken).orElse(null);
        if (refreshToken == null) {
            return;
        }

        refreshToken.setRevocado(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse generarRespuestaAuth(Usuario usuario) {
        return generarRespuestaAuth(usuario, UUID.randomUUID().toString());
    }

    private AuthResponse generarRespuestaAuth(Usuario usuario, String sessionId) {
        UsuarioDetailsImpl userDetails = new UsuarioDetailsImpl(usuario);
        String accessToken = jwtService.generarToken(userDetails);
        String refreshTokenValor = crearRefreshToken(usuario, sessionId);
        String rol = Hibernate.unproxy(usuario).getClass().getSimpleName();

        return new AuthResponse(accessToken, refreshTokenValor, "Bearer", usuario.getCorreo(), rol);
    }

    private String crearRefreshToken(Usuario usuario, String sessionId) {
        String valor = UUID.randomUUID().toString() + UUID.randomUUID();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(hash(valor));
        refreshToken.setUsuario(usuario);
        refreshToken.setSessionId(sessionId);
        refreshToken.setFechaExpiracion(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000));
        refreshTokenRepository.save(refreshToken);
        return valor;
    }

    private String validarRefreshRequest(RefreshTokenRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw new CredencialesInvalidasException("Refresh token invalido");
        }
        return request.getRefreshToken();
    }

    private java.util.Optional<RefreshToken> buscarRefreshToken(String valor) {
        String hash = hash(valor);
        return refreshTokenRepository.findByTokenHash(hash)
                // Compatibilidad de transición para datos antiguos almacenados en claro.
                .or(() -> refreshTokenRepository.findByToken(valor));
    }

    private String hash(String valor) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(64);
            for (byte b : digest) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo proteger la sesion");
        }
    }
}