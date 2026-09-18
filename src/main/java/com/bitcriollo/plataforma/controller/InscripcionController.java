package com.bitcriollo.plataforma.controller;

import com.bitcriollo.plataforma.dto.InscripcionRequest;
import com.bitcriollo.plataforma.dto.InscripcionResponse;
import com.bitcriollo.plataforma.model.Estudiante;
import com.bitcriollo.plataforma.security.UsuarioDetailsImpl;
import com.bitcriollo.plataforma.service.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscripciones")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    public InscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    @PostMapping
    public ResponseEntity<InscripcionResponse> inscribirse(@Valid @RequestBody InscripcionRequest request,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        Estudiante estudiante = (Estudiante) userDetails.getUsuario();
        InscripcionResponse response = inscripcionService.inscribirse(request, estudiante);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarInscripcion(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        Estudiante estudiante = (Estudiante) userDetails.getUsuario();
        inscripcionService.cancelarInscripcion(id, estudiante);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mis-cursos")
    public ResponseEntity<List<InscripcionResponse>> misCursos(
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        Estudiante estudiante = (Estudiante) userDetails.getUsuario();
        return ResponseEntity.ok(inscripcionService.misCursos(estudiante));
    }
}