package com.bitcriollo.plataforma.controller;

import com.bitcriollo.plataforma.dto.EvaluacionResponse;
import com.bitcriollo.plataforma.dto.ExamenRequest;
import com.bitcriollo.plataforma.dto.ExamenResponse;
import com.bitcriollo.plataforma.dto.TareaRequest;
import com.bitcriollo.plataforma.dto.TareaResponse;
import com.bitcriollo.plataforma.security.UsuarioDetailsImpl;
import com.bitcriollo.plataforma.service.EvaluacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EvaluacionController {

    private final EvaluacionService evaluacionService;

    public EvaluacionController(EvaluacionService evaluacionService) {
        this.evaluacionService = evaluacionService;
    }

    @GetMapping("/cursos/{cursoId}/evaluaciones")
    public ResponseEntity<List<EvaluacionResponse>> listarEvaluaciones(@PathVariable Long cursoId,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        return ResponseEntity.ok(evaluacionService.listarPorCurso(cursoId, userDetails.getUsuario()));
    }

    @PostMapping("/cursos/{cursoId}/examenes")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<ExamenResponse> crearExamen(@PathVariable Long cursoId,
            @Valid @RequestBody ExamenRequest request,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        ExamenResponse response = evaluacionService.crearExamen(cursoId, request, userDetails.getUsuario());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/examenes/{id}")
    public ResponseEntity<ExamenResponse> obtenerExamen(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        return ResponseEntity.ok(evaluacionService.obtenerExamen(id, userDetails.getUsuario()));
    }

    @PostMapping("/cursos/{cursoId}/tareas")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<TareaResponse> crearTarea(@PathVariable Long cursoId,
            @Valid @RequestBody TareaRequest request,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        TareaResponse response = evaluacionService.crearTarea(cursoId, request, userDetails.getUsuario());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tareas/{id}")
    public ResponseEntity<TareaResponse> obtenerTarea(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        return ResponseEntity.ok(evaluacionService.obtenerTarea(id, userDetails.getUsuario()));
    }
}
