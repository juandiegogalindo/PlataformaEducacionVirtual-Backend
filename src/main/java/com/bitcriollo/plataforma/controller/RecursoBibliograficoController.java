package com.bitcriollo.plataforma.controller;

import com.bitcriollo.plataforma.dto.RecursoBibliograficoRequest;
import com.bitcriollo.plataforma.dto.RecursoBibliograficoResponse;
import com.bitcriollo.plataforma.security.UsuarioDetailsImpl;
import com.bitcriollo.plataforma.service.RecursoBibliograficoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos/{cursoId}/recursos")
public class RecursoBibliograficoController {

    private final RecursoBibliograficoService recursoService;

    public RecursoBibliograficoController(RecursoBibliograficoService recursoService) {
        this.recursoService = recursoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<RecursoBibliograficoResponse> crearRecurso(@PathVariable Long cursoId,
            @Valid @RequestBody RecursoBibliograficoRequest request,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        RecursoBibliograficoResponse response = recursoService.crearRecurso(cursoId, request, userDetails.getUsuario());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RecursoBibliograficoResponse>> listarRecursos(@PathVariable Long cursoId,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        return ResponseEntity.ok(recursoService.listarRecursos(cursoId, userDetails.getUsuario()));
    }

    @PutMapping("/{recursoId}")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<RecursoBibliograficoResponse> actualizarRecurso(@PathVariable Long cursoId,
            @PathVariable Long recursoId,
            @Valid @RequestBody RecursoBibliograficoRequest request,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        RecursoBibliograficoResponse response = recursoService.actualizarRecurso(cursoId, recursoId, request,
                userDetails.getUsuario());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{recursoId}")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<Void> eliminarRecurso(@PathVariable Long cursoId,
            @PathVariable Long recursoId,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        recursoService.eliminarRecurso(cursoId, recursoId, userDetails.getUsuario());
        return ResponseEntity.noContent().build();
    }
}