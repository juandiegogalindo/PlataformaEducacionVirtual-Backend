package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.ProgresoLeccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgresoLeccionRepository extends JpaRepository<ProgresoLeccion, Long> {
    List<ProgresoLeccion> findByEstudianteId(Long estudianteId);

    Optional<ProgresoLeccion> findByEstudianteIdAndLeccionId(Long estudianteId, Long leccionId);
}