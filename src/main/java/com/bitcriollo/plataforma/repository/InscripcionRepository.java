package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    List<Inscripcion> findByEstudianteId(Long estudianteId);

    List<Inscripcion> findByCursoId(Long cursoId);

    Optional<Inscripcion> findByEstudianteIdAndCursoId(Long estudianteId, Long cursoId);
}