package com.bitcriollo.plataforma.controller;

import com.bitcriollo.plataforma.dto.LeccionRequest;
import com.bitcriollo.plataforma.dto.LeccionResponse;
import com.bitcriollo.plataforma.security.UsuarioDetailsImpl;
import com.bitcriollo.plataforma.service.LeccionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos/{cursoId}/lecciones")
public class LeccionController {

    private final LeccionService leccionService;

    public LeccionController(LeccionService leccionService) {
        this.leccionService = leccionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<LeccionResponse> crearLeccion(@PathVariable Long cursoId,
            @Valid @RequestBody LeccionRequest request,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        LeccionResponse response = leccionService.crearLeccion(cursoId, request, userDetails.getUsuario());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<LeccionResponse>> listarLecciones(@PathVariable Long cursoId,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        return ResponseEntity.ok(leccionService.listarLecciones(cursoId, userDetails.getUsuario()));
    }

    @PutMapping("/{leccionId}")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<LeccionResponse> actualizarLeccion(@PathVariable Long cursoId,
            @PathVariable Long leccionId,
            @Valid @RequestBody LeccionRequest request,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        LeccionResponse response = leccionService.actualizarLeccion(cursoId, leccionId, request,
                userDetails.getUsuario());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{leccionId}")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<Void> eliminarLeccion(@PathVariable Long cursoId,
            @PathVariable Long leccionId,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        leccionService.eliminarLeccion(cursoId, leccionId, userDetails.getUsuario());
        return ResponseEntity.noContent().build();
    }
}