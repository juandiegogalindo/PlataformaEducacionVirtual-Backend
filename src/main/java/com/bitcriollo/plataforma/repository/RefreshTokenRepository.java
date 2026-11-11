package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // Se conserva para que las pruebas y despliegues antiguos puedan migrar sin romper el contrato Java.
    @Query("select r from RefreshToken r where r.tokenHash = :token")
    Optional<RefreshToken> findByToken(@Param("token") String token);

    List<RefreshToken> findByUsuarioIdAndRevocadoFalse(Long usuarioId);

    List<RefreshToken> findBySessionId(String sessionId);
}