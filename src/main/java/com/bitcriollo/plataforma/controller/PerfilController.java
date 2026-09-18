package com.bitcriollo.plataforma.controller;

import com.bitcriollo.plataforma.dto.ActualizarPerfilRequest;
import com.bitcriollo.plataforma.dto.UsuarioResponse;
import com.bitcriollo.plataforma.security.UsuarioDetailsImpl;
import com.bitcriollo.plataforma.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bitcriollo.plataforma.dto.CambiarContrasenaRequest;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<UsuarioResponse> obtenerPerfil(@AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        return ResponseEntity.ok(usuarioService.obtenerPerfil(userDetails.getUsuario()));
    }

    @PatchMapping
    public ResponseEntity<UsuarioResponse> actualizarPerfil(@AuthenticationPrincipal UsuarioDetailsImpl userDetails,
            @Valid @RequestBody ActualizarPerfilRequest request) {
        UsuarioResponse response = usuarioService.actualizarPerfil(userDetails.getUsuario(), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/contrasena")
    public ResponseEntity<Void> cambiarContrasena(@AuthenticationPrincipal UsuarioDetailsImpl userDetails,
            @Valid @RequestBody CambiarContrasenaRequest request) {
        usuarioService.cambiarContrasena(userDetails.getUsuario(), request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}