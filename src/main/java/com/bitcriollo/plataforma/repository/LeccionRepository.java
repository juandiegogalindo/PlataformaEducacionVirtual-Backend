package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.Leccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeccionRepository extends JpaRepository<Leccion, Long> {
    List<Leccion> findByCursoIdOrderByOrdenAsc(Long cursoId);
}