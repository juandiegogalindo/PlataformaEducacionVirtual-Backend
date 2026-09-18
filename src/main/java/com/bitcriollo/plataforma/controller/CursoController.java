package com.bitcriollo.plataforma.controller;

import com.bitcriollo.plataforma.dto.CursoRequest;
import com.bitcriollo.plataforma.dto.CursoResponse;
import com.bitcriollo.plataforma.model.Usuario;
import com.bitcriollo.plataforma.security.UsuarioDetailsImpl;
import com.bitcriollo.plataforma.service.CursoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCENTE') or hasRole('COORDINADORACADEMICO')")
    public ResponseEntity<CursoResponse> crearCurso(@Valid @RequestBody CursoRequest request,
                                                      @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        Usuario creador = userDetails.getUsuario();
        CursoResponse response = cursoService.crearCurso(request, creador);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CursoResponse>> listarCursos() {
        return ResponseEntity.ok(cursoService.listarCursos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursoResponse> obtenerCurso(@PathVariable Long id) {
        return ResponseEntity.ok(cursoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CursoResponse> actualizarCurso(@PathVariable Long id,
                                                           @Valid @RequestBody CursoRequest request,
                                                           @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        CursoResponse response = cursoService.actualizarCurso(id, request, userDetails.getUsuario());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archivarCurso(@PathVariable Long id,
                                               @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        cursoService.archivarCurso(id, userDetails.getUsuario());
        return ResponseEntity.noContent().build();
    }
}