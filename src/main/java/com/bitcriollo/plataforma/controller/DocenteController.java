package com.bitcriollo.plataforma.controller;

import com.bitcriollo.plataforma.dto.CursoResponse;
import com.bitcriollo.plataforma.dto.DocenteResumenResponse;
import com.bitcriollo.plataforma.model.enums.EstadoCurso;
import com.bitcriollo.plataforma.security.UsuarioDetailsImpl;
import com.bitcriollo.plataforma.service.CursoService;
import com.bitcriollo.plataforma.service.DocenteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/docentes")
public class DocenteController {

    private final CursoService cursoService;
    private final DocenteService docenteService;

    public DocenteController(CursoService cursoService, DocenteService docenteService) {
        this.cursoService = cursoService;
        this.docenteService = docenteService;
    }

    @GetMapping
    @PreAuthorize("hasRole('COORDINADORACADEMICO') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<DocenteResumenResponse>> listarDocentesActivos() {
        return ResponseEntity.ok(docenteService.listarDocentesActivos());
    }

    @GetMapping("/mis-cursos")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<List<CursoResponse>> misCursos(@RequestParam(required = false) EstadoCurso estado,
                                                           @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        return ResponseEntity.ok(cursoService.listarCursosDeDocente(userDetails.getUsuario().getId(), estado));
    }
}