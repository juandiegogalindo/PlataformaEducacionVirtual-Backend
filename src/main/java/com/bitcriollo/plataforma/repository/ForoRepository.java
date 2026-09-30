package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.Foro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ForoRepository extends JpaRepository<Foro, Long> {
    Optional<Foro> findByCursoId(Long cursoId);
}