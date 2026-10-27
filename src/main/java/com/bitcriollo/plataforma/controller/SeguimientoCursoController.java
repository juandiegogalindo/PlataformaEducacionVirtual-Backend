package com.bitcriollo.plataforma.controller;

import com.bitcriollo.plataforma.dto.CursoProgresoResponse;
import com.bitcriollo.plataforma.dto.EstudianteInscritoResponse;
import com.bitcriollo.plataforma.model.enums.EstadoInscripcion;
import com.bitcriollo.plataforma.security.UsuarioDetailsImpl;
import com.bitcriollo.plataforma.service.SeguimientoCursoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cursos/{cursoId}/estudiantes")
@PreAuthorize("hasRole('DOCENTE') or hasRole('COORDINADORACADEMICO') or hasRole('ADMINISTRADOR')")
public class SeguimientoCursoController {

    private final SeguimientoCursoService seguimientoCursoService;

    public SeguimientoCursoController(SeguimientoCursoService seguimientoCursoService) {
        this.seguimientoCursoService = seguimientoCursoService;
    }

    @GetMapping
    public ResponseEntity<List<EstudianteInscritoResponse>> listarInscritos(
            @PathVariable Long cursoId,
            @RequestParam(required = false) EstadoInscripcion estado,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        return ResponseEntity.ok(seguimientoCursoService.listarInscritos(cursoId, estado, userDetails.getUsuario()));
    }

    @GetMapping("/{estudianteId}/progreso")
    public ResponseEntity<CursoProgresoResponse> progresoDeEstudiante(
            @PathVariable Long cursoId,
            @PathVariable Long estudianteId,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        return ResponseEntity.ok(
                seguimientoCursoService.obtenerProgresoDeEstudiante(cursoId, estudianteId, userDetails.getUsuario()));
    }
}