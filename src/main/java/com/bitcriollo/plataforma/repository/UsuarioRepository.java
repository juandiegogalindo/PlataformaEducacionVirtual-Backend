package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u.id from Usuario u where u.id = :id")
    Optional<Long> bloquearFilaParaAuditoria(Long id);
}