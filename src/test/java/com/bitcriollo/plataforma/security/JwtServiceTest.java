package com.bitcriollo.plataforma.security;

import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {
    private JwtService servicio(String clave, long duracion) {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", clave);
        ReflectionTestUtils.setField(service, "expirationMs", duracion);
        return service;
    }

    @Test
    void rechazaUnaClaveDebilAlIniciar() {
        assertThatThrownBy(() -> servicio("corta", 60000).validarConfiguracion())
                .isInstanceOf(WeakKeyException.class);
    }

    @Test
    void rechazaUnaDuracionNoPositiva() {
        assertThatThrownBy(() -> servicio("a".repeat(32), 0).validarConfiguracion())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void firmaYVerificaUsandoUtf8() {
        JwtService service = servicio("clave-ñ-segura-para-pruebas-".repeat(2), 60000);
        service.validarConfiguracion();
        var usuario = User.withUsername("estudiante@test.local").password("hash").roles("ESTUDIANTE").build();
        String token = service.generarToken(usuario);
        assertThat(service.extraerCorreo(token)).isEqualTo(usuario.getUsername());
        assertThat(service.esTokenValido(token, usuario)).isTrue();
    }

    @Test
    void noAceptaElTokenDeOtroUsuario() {
        JwtService service = servicio("a".repeat(32), 60000);
        var primero = User.withUsername("uno").password("hash").roles("ESTUDIANTE").build();
        var segundo = User.withUsername("dos").password("hash").roles("ESTUDIANTE").build();
        assertThat(service.esTokenValido(service.generarToken(primero), segundo)).isFalse();
    }
}
