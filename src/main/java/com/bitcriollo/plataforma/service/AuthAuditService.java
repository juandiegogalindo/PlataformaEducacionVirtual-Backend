package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.model.LogAcceso;
import com.bitcriollo.plataforma.model.Usuario;
import com.bitcriollo.plataforma.repository.LogAccesoRepository;
import com.bitcriollo.plataforma.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthAuditService {

    private static final int MAX_INTENTOS_FALLIDOS = 3;
    private static final long MINUTOS_BLOQUEO = 15;

    private final UsuarioRepository usuarioRepository;
    private final LogAccesoRepository logAccesoRepository;

    public AuthAuditService(UsuarioRepository usuarioRepository, LogAccesoRepository logAccesoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.logAccesoRepository = logAccesoRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarIntentoFallido(Usuario usuario) {
        usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);
        // Después de tres intentos fallidos, el usuario queda bloqueado durante 15 minutos.
        if (usuario.getIntentosFallidos() >= MAX_INTENTOS_FALLIDOS) {
            usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
        }
        usuarioRepository.save(usuario);
    }
    // El registro de auditoría se ejecuta en una transacción independiente para conservar el evento.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarLogAcceso(Usuario usuario, String correoIntento, boolean exitoso, String ipOrigen) {
        LogAcceso log = new LogAcceso();
        log.setUsuario(usuario);
        log.setCorreoIntento(correoIntento);
        log.setExitoso(exitoso);
        log.setIpOrigen(ipOrigen);
        logAccesoRepository.save(log);
    }
}