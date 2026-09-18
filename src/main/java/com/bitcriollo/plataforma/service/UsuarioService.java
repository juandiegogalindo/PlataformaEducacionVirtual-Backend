package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.ActualizarPerfilRequest;
import com.bitcriollo.plataforma.dto.UsuarioResponse;
import com.bitcriollo.plataforma.model.Usuario;
import com.bitcriollo.plataforma.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.bitcriollo.plataforma.dto.CambiarContrasenaRequest;
import com.bitcriollo.plataforma.repository.RefreshTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfil(Usuario usuario) {
        return mapearAResponse(usuario);
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(Usuario usuario, ActualizarPerfilRequest request) {
        if (StringUtils.hasText(request.getNombre())) {
            usuario.setNombre(request.getNombre());
        }
        if (StringUtils.hasText(request.getApellido())) {
            usuario.setApellido(request.getApellido());
        }
        if (StringUtils.hasText(request.getTelefono())) {
            usuario.setTelefono(request.getTelefono());
        }
        if (StringUtils.hasText(request.getFotoPerfilUrl())) {
            usuario.setFotoPerfilUrl(request.getFotoPerfilUrl());
        }

        usuarioRepository.save(usuario);
        return mapearAResponse(usuario);
    }

    @Transactional
    public void cambiarContrasena(Usuario usuario, CambiarContrasenaRequest request) {
        if (!passwordEncoder.matches(request.getContrasenaActual(), usuario.getContrasenaHash())) {
            throw new IllegalArgumentException("La contrasena actual no es correcta");
        }

        if (!request.getContrasenaNueva().equals(request.getConfirmarContrasenaNueva())) {
            throw new IllegalArgumentException("Las contrasenas nuevas no coinciden");
        }

        if (passwordEncoder.matches(request.getContrasenaNueva(), usuario.getContrasenaHash())) {
            throw new IllegalArgumentException("La contrasena nueva debe ser diferente a la actual");
        }

        usuario.setContrasenaHash(passwordEncoder.encode(request.getContrasenaNueva()));
        usuarioRepository.save(usuario);

        refreshTokenRepository.findAll().stream()
                .filter(rt -> rt.getUsuario().getId().equals(usuario.getId()) && !rt.isRevocado())
                .forEach(rt -> {
                    rt.setRevocado(true);
                    refreshTokenRepository.save(rt);
                });
    }

    private UsuarioResponse mapearAResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getClass().getSimpleName(),
                usuario.isActivo(),
                usuario.getCreatedAt(),
                usuario.getTelefono(),
                usuario.getFotoPerfilUrl());
    }
}