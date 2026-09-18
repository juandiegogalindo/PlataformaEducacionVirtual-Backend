package com.bitcriollo.plataforma.controller;

import com.bitcriollo.plataforma.dto.AdministradorRequest;
import com.bitcriollo.plataforma.dto.CoordinadorRequest;
import com.bitcriollo.plataforma.dto.DocenteRequest;
import com.bitcriollo.plataforma.dto.UsuarioResponse;
import com.bitcriollo.plataforma.service.AdminUsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminController {

    private final AdminUsuarioService adminUsuarioService;

    public AdminController(AdminUsuarioService adminUsuarioService) {
        this.adminUsuarioService = adminUsuarioService;
    }

    @PostMapping("/docentes")
    public ResponseEntity<UsuarioResponse> crearDocente(@Valid @RequestBody DocenteRequest request) {
        UsuarioResponse response = adminUsuarioService.crearDocente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/administradores")
    public ResponseEntity<UsuarioResponse> crearAdministrador(@Valid @RequestBody AdministradorRequest request) {
        UsuarioResponse response = adminUsuarioService.crearAdministrador(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/coordinadores")
    public ResponseEntity<UsuarioResponse> crearCoordinador(@Valid @RequestBody CoordinadorRequest request) {
        UsuarioResponse response = adminUsuarioService.crearCoordinador(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}