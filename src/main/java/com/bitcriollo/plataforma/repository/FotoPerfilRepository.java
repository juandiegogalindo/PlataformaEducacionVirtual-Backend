package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.FotoPerfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FotoPerfilRepository extends JpaRepository<FotoPerfil, Long> {
    Optional<FotoPerfil> findByUsuarioId(Long usuarioId);
}