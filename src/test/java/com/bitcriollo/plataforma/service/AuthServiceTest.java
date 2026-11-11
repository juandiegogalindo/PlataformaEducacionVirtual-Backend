package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.RefreshTokenRequest;
import com.bitcriollo.plataforma.exception.CredencialesInvalidasException;
import com.bitcriollo.plataforma.model.Estudiante;
import com.bitcriollo.plataforma.model.RefreshToken;
import com.bitcriollo.plataforma.repository.EstudianteRepository;
import com.bitcriollo.plataforma.repository.RefreshTokenRepository;
import com.bitcriollo.plataforma.repository.UsuarioRepository;
import com.bitcriollo.plataforma.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UsuarioRepository usuarios;
    @Mock private EstudianteRepository estudiantes;
    @Mock private RefreshTokenRepository tokens;
    @Mock private PasswordEncoder encoder;
    @Mock private JwtService jwt;
    @Mock private AuthAuditService auditoria;
    @InjectMocks private AuthService service;

    private RefreshTokenRequest request;
    private RefreshToken token;
    private Estudiante estudiante;

    @BeforeEach
    void preparar() {
        ReflectionTestUtils.setField(service, "refreshExpirationMs", 604800000L);
        request = new RefreshTokenRequest();
        request.setRefreshToken("sesion-original");
        estudiante = new Estudiante();
        estudiante.setId(1L);
        estudiante.setCorreo("estudiante@test.local");
        token = new RefreshToken();
        token.setUsuario(estudiante);
        token.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        when(tokens.findByToken("sesion-original")).thenReturn(Optional.of(token));
    }

    @Test
    void noRenuevaUnaCuentaDesactivada() {
        estudiante.setActivo(false);
        assertThatThrownBy(() -> service.refrescarToken(request)).isInstanceOf(CredencialesInvalidasException.class);
        verify(tokens, never()).save(any());
        verifyNoInteractions(jwt);
    }

    @Test
    void noRenuevaUnaCuentaBloqueada() {
        estudiante.setBloqueadoHasta(LocalDateTime.now().plusMinutes(10));
        assertThatThrownBy(() -> service.refrescarToken(request)).isInstanceOf(CredencialesInvalidasException.class);
        verify(tokens, never()).save(any());
    }

    @Test
    void noRenuevaUnTokenExpirado() {
        token.setFechaExpiracion(LocalDateTime.now().minusSeconds(1));
        assertThatThrownBy(() -> service.refrescarToken(request)).isInstanceOf(CredencialesInvalidasException.class);
        verifyNoInteractions(jwt);
    }

    @Test
    void noReutilizaUnTokenRevocado() {
        token.setRevocado(true);
        assertThatThrownBy(() -> service.refrescarToken(request)).isInstanceOf(CredencialesInvalidasException.class);
        verifyNoInteractions(jwt);
    }

    @Test
    void unTokenInexistenteEsUnFalloDeAutenticacion() {
        when(tokens.findByToken("sesion-original")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.refrescarToken(request)).isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void renuevaUnaSesionValidaYRevocaLaAnterior() {
        when(jwt.generarToken(any())).thenReturn("nuevo-jwt");
        var respuesta = service.refrescarToken(request);
        assertThat(token.isRevocado()).isTrue();
        assertThat(respuesta.getAccessToken()).isEqualTo("nuevo-jwt");
        assertThat(respuesta.getRol()).isEqualTo("Estudiante");
        assertThat(respuesta.getRefreshToken()).isNotBlank().isNotEqualTo(request.getRefreshToken());
        verify(tokens, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void permiteRenovarCuandoYaFinalizoElBloqueo() {
        estudiante.setBloqueadoHasta(LocalDateTime.now().minusMinutes(1));
        assertThatCode(() -> service.refrescarToken(request)).doesNotThrowAnyException();
    }
}
