package com.bitcriollo.plataforma.controller;

import com.bitcriollo.plataforma.dto.CalificarResultadoRequest;
import com.bitcriollo.plataforma.dto.RegistrarResultadoRequest;
import com.bitcriollo.plataforma.dto.ResultadoResponse;
import com.bitcriollo.plataforma.security.UsuarioDetailsImpl;
import com.bitcriollo.plataforma.service.ResultadoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ResultadoController {

    private final ResultadoService resultadoService;

    public ResultadoController(ResultadoService resultadoService) {
        this.resultadoService = resultadoService;
    }

    @PostMapping("/evaluaciones/{evaluacionId}/resultados")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<ResultadoResponse> registrarResultado(@PathVariable Long evaluacionId,
            @RequestBody(required = false) RegistrarResultadoRequest request,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        RegistrarResultadoRequest cuerpo = request != null ? request : new RegistrarResultadoRequest();
        ResultadoResponse response = resultadoService.registrarResultado(evaluacionId, cuerpo,
                userDetails.getUsuario());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/resultados/{id}/calificar")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<ResultadoResponse> calificar(@PathVariable Long id,
            @Valid @RequestBody CalificarResultadoRequest request,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        return ResponseEntity.ok(resultadoService.calificar(id, request, userDetails.getUsuario()));
    }
}
