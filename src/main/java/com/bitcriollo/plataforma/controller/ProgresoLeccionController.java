package com.bitcriollo.plataforma.controller;

import com.bitcriollo.plataforma.dto.CursoProgresoResponse;
import com.bitcriollo.plataforma.dto.ProgresoLeccionRequest;
import com.bitcriollo.plataforma.dto.ProgresoLeccionResponse;
import com.bitcriollo.plataforma.model.Estudiante;
import com.bitcriollo.plataforma.security.UsuarioDetailsImpl;
import com.bitcriollo.plataforma.service.ProgresoLeccionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cursos/{cursoId}")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class ProgresoLeccionController {

    private final ProgresoLeccionService progresoLeccionService;

    public ProgresoLeccionController(ProgresoLeccionService progresoLeccionService) {
        this.progresoLeccionService = progresoLeccionService;
    }

    @PatchMapping("/lecciones/{leccionId}/progreso")
    public ResponseEntity<ProgresoLeccionResponse> marcarProgreso(@PathVariable Long cursoId,
            @PathVariable Long leccionId,
            @Valid @RequestBody ProgresoLeccionRequest request,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        Estudiante estudiante = (Estudiante) userDetails.getUsuario();
        return ResponseEntity.ok(progresoLeccionService.marcarProgreso(cursoId, leccionId, request, estudiante));
    }

    @GetMapping("/progreso")
    public ResponseEntity<CursoProgresoResponse> obtenerProgreso(@PathVariable Long cursoId,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        Estudiante estudiante = (Estudiante) userDetails.getUsuario();
        return ResponseEntity.ok(progresoLeccionService.obtenerProgresoCurso(cursoId, estudiante));
    }
}