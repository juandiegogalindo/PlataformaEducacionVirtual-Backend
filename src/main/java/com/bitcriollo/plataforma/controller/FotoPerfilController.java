package com.bitcriollo.plataforma.controller;

import com.bitcriollo.plataforma.dto.UsuarioResponse;
import com.bitcriollo.plataforma.model.Usuario;
import com.bitcriollo.plataforma.security.UsuarioDetailsImpl;
import com.bitcriollo.plataforma.service.FotoPerfilService;
import com.bitcriollo.plataforma.service.UsuarioService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FotoPerfilController {

    private final FotoPerfilService fotoPerfilService;
    private final UsuarioService usuarioService;

    public FotoPerfilController(FotoPerfilService fotoPerfilService, UsuarioService usuarioService) {
        this.fotoPerfilService = fotoPerfilService;
        this.usuarioService = usuarioService;
    }

    @PutMapping(value = "/api/perfil/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioResponse> subirFoto(@RequestParam("foto") MultipartFile foto,
            @AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        Usuario actualizado = fotoPerfilService.subirFoto(userDetails.getUsuario(), foto);
        return ResponseEntity.ok(usuarioService.obtenerPerfil(actualizado));
    }

    @DeleteMapping("/api/perfil/foto")
    public ResponseEntity<UsuarioResponse> eliminarFoto(@AuthenticationPrincipal UsuarioDetailsImpl userDetails) {
        Usuario actualizado = fotoPerfilService.eliminarFoto(userDetails.getUsuario());
        return ResponseEntity.ok(usuarioService.obtenerPerfil(actualizado));
    }

    // Publico: se usa directo en <img src="...">, que no puede enviar el token.
    @GetMapping("/api/usuarios/{usuarioId}/foto")
    public ResponseEntity<byte[]> obtenerFoto(@PathVariable Long usuarioId) {
        return fotoPerfilService.obtenerFoto(usuarioId)
                .map(foto -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(foto.getTipoContenido()))
                        // La URL no cambia al reemplazar la foto: el navegador debe revalidar con el ETag.
                        .cacheControl(CacheControl.noCache())
                        .eTag("\"" + foto.getUpdatedAt() + "\"")
                        .body(foto.getContenido()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}