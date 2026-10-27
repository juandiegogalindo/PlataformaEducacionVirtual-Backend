package com.bitcriollo.plataforma.controller;

import com.bitcriollo.plataforma.dto.CursoResponse;
import com.bitcriollo.plataforma.model.enums.EstadoCurso;
import com.bitcriollo.plataforma.service.CursoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gestion/cursos")
public class GestionCursoController {

    private final CursoService cursoService;

    public GestionCursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping
    @PreAuthorize("hasRole('COORDINADORACADEMICO') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<CursoResponse>> listarParaGestion(
            @RequestParam(required = false) EstadoCurso estado) {
        return ResponseEntity.ok(cursoService.listarCursosParaGestion(estado));
    }
}