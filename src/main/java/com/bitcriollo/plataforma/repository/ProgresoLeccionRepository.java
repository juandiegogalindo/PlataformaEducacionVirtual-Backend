package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.ProgresoLeccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProgresoLeccionRepository extends JpaRepository<ProgresoLeccion, Long> {
    List<ProgresoLeccion> findByEstudianteId(Long estudianteId);

    Optional<ProgresoLeccion> findByEstudianteIdAndLeccionId(Long estudianteId, Long leccionId);

        // Devuelve pares [estudianteId, cantidad de lecciones completadas] para todos los estudiantes del curso.
    @Query("select p.estudiante.id, count(p) from ProgresoLeccion p "
            + "where p.leccion.curso.id = :cursoId and p.completado = true group by p.estudiante.id")
    List<Object[]> contarCompletadasPorEstudiante(@Param("cursoId") Long cursoId);
}