package com.bitcriollo.plataforma.controller;

import com.bitcriollo.plataforma.dto.MensajeForoRequest;
import com.bitcriollo.plataforma.dto.MensajeForoResponse;
import com.bitcriollo.plataforma.security.UsuarioDetailsImpl;
import com.bitcriollo.plataforma.service.MensajeForoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Solo Docente y Estudiante pueden llegar a participar en el foro (ver MensajeForoService); el
// control de acceso especifico por curso/mensaje sigue resuelto en el servicio.
@RestController
@RequestMapping("/api/cursos/{cursoId}/foro/mensajes")
@PreAuthorize("hasRole('DOCENTE') or hasRole('ESTUDIANTE')")
public class MensajeForoController {

    private final MensajeForoService mensajeForoService;

    public MensajeForoController(MensajeForoService mensajeForoService) {
        this.mensajeForoService = mensajeForoService;
    }

    @GetMapping
    public ResponseEntity<List<MensajeForoResponse>> listarMensajes(@PathVariable Long cursoId,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        return ResponseEntity.ok(mensajeForoService.listarMensajes(cursoId, userDetails.getUsuario()));
    }

    @PostMapping
    public ResponseEntity<MensajeForoResponse> publicarMensaje(@PathVariable Long cursoId,
            @Valid @RequestBody MensajeForoRequest request,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        MensajeForoResponse response = mensajeForoService.publicarMensaje(cursoId, request, userDetails.getUsuario());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{mensajeId}")
    public ResponseEntity<MensajeForoResponse> editarMensaje(@PathVariable Long cursoId,
            @PathVariable Long mensajeId,
            @Valid @RequestBody MensajeForoRequest request,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        MensajeForoResponse response = mensajeForoService.editarMensaje(cursoId, mensajeId, request,
                userDetails.getUsuario());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{mensajeId}")
    public ResponseEntity<Void> eliminarMensaje(@PathVariable Long cursoId,
            @PathVariable Long mensajeId,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        mensajeForoService.eliminarMensaje(cursoId, mensajeId, userDetails.getUsuario());
        return ResponseEntity.noContent().build();
    }
}