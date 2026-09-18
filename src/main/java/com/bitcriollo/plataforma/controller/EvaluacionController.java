package com.bitcriollo.plataforma.controller;

import com.bitcriollo.plataforma.dto.EvaluacionResponse;
import com.bitcriollo.plataforma.security.UsuarioDetailsImpl;
import com.bitcriollo.plataforma.service.EvaluacionService;
import org.springframework.http.ResponseEntity;
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
}
