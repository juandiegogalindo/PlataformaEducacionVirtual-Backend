package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.model.FotoPerfil;
import com.bitcriollo.plataforma.model.Usuario;
import com.bitcriollo.plataforma.repository.FotoPerfilRepository;
import com.bitcriollo.plataforma.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class FotoPerfilService {

    private static final long TAMANIO_MAXIMO_BYTES = 5L * 1024 * 1024;

    private final FotoPerfilRepository fotoPerfilRepository;
    private final UsuarioRepository usuarioRepository;

    public FotoPerfilService(FotoPerfilRepository fotoPerfilRepository, UsuarioRepository usuarioRepository) {
        this.fotoPerfilRepository = fotoPerfilRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Usuario subirFoto(Usuario usuario, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Debes enviar una imagen");
        }
        if (archivo.getSize() > TAMANIO_MAXIMO_BYTES) {
            throw new IllegalArgumentException("La imagen supera el tamano maximo de 5 MB");
        }

        byte[] contenido;
        try {
            contenido = archivo.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer la imagen enviada");
        }

        // No se confia en el tipo que declara el cliente: se detecta por los primeros bytes del archivo.
        String tipo = detectarTipoImagen(contenido);
        if (tipo == null) {
            throw new IllegalArgumentException("Formato no permitido: solo se aceptan imagenes JPEG, PNG o WebP");
        }

        // Cada usuario tiene como maximo una foto: si ya existe, se reemplaza.
        FotoPerfil foto = fotoPerfilRepository.findByUsuarioId(usuario.getId()).orElseGet(() -> {
            FotoPerfil nueva = new FotoPerfil();
            nueva.setUsuario(usuario);
            return nueva;
        });
        foto.setContenido(contenido);
        foto.setTipoContenido(tipo);
        foto.setTamanioBytes(contenido.length);
        fotoPerfilRepository.save(foto);

        usuario.setFotoPerfilUrl(urlInterna(usuario.getId()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario eliminarFoto(Usuario usuario) {
        fotoPerfilRepository.findByUsuarioId(usuario.getId()).ifPresent(fotoPerfilRepository::delete);

        // Solo se limpia la URL si apuntaba a la foto guardada aqui (no a un enlace externo).
        if (urlInterna(usuario.getId()).equals(usuario.getFotoPerfilUrl())) {
            usuario.setFotoPerfilUrl(null);
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Optional<FotoPerfil> obtenerFoto(Long usuarioId) {
        return fotoPerfilRepository.findByUsuarioId(usuarioId);
    }

    private String urlInterna(Long usuarioId) {
        return "/api/usuarios/" + usuarioId + "/foto";
    }

    private String detectarTipoImagen(byte[] b) {
        // JPEG: FF D8 FF
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "image/png";
        }
        // WebP: "RIFF" + 4 bytes de tamano + "WEBP"
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return null;
    }
}