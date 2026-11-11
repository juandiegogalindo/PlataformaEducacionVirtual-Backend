package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.model.Estudiante;
import com.bitcriollo.plataforma.repository.LogAccesoRepository;
import com.bitcriollo.plataforma.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthAuditServiceTest {
    @Mock private UsuarioRepository usuarios;
    @Mock private LogAccesoRepository logs;
    @InjectMocks private AuthAuditService service;
    private Estudiante original;
    private Estudiante actual;

    @BeforeEach
    void preparar() {
        original = new Estudiante();
        original.setId(1L);
        actual = new Estudiante();
        actual.setId(1L);
        when(usuarios.bloquearFilaParaAuditoria(1L)).thenReturn(Optional.of(1L));
        when(usuarios.findById(1L)).thenReturn(Optional.of(actual));
    }

    @Test
    void incrementaElValorPersistidoSinModificarLaEntidadDeLogin() {
        actual.setIntentosFallidos(1);
        service.registrarIntentoFallido(original);
        assertThat(actual.getIntentosFallidos()).isEqualTo(2);
        assertThat(original.getIntentosFallidos()).isZero();
        verify(usuarios).save(actual);
    }

    @Test
    void elTercerFalloBloqueaDuranteQuinceMinutos() {
        actual.setIntentosFallidos(2);
        LocalDateTime antes = LocalDateTime.now();
        service.registrarIntentoFallido(original);
        assertThat(actual.getIntentosFallidos()).isEqualTo(3);
        assertThat(actual.getBloqueadoHasta()).isBetween(antes.plusMinutes(15), LocalDateTime.now().plusMinutes(15));
    }

    @Test
    void unBloqueoVencidoIniciaUnNuevoCicloDeIntentos() {
        actual.setIntentosFallidos(3);
        actual.setBloqueadoHasta(LocalDateTime.now().minusMinutes(1));
        service.registrarIntentoFallido(original);
        assertThat(actual.getIntentosFallidos()).isEqualTo(1);
        assertThat(actual.getBloqueadoHasta()).isNull();
    }

    @Test
    void noExtiendeUnBloqueoVigente() {
        LocalDateTime limite = LocalDateTime.now().plusMinutes(5);
        actual.setIntentosFallidos(3);
        actual.setBloqueadoHasta(limite);
        service.registrarIntentoFallido(original);
        assertThat(actual.getBloqueadoHasta()).isEqualTo(limite);
        assertThat(actual.getIntentosFallidos()).isEqualTo(3);
        verify(usuarios, never()).save(any());
    }
}
