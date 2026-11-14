package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.Inscripcion;
import com.bitcriollo.plataforma.model.enums.EstadoInscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    List<Inscripcion> findByEstudianteId(Long estudianteId);

    List<Inscripcion> findByCursoId(Long cursoId);

    Optional<Inscripcion> findByEstudianteIdAndCursoId(Long estudianteId, Long cursoId);

    long countByCursoIdAndEstado(Long cursoId, EstadoInscripcion estado);

    @Query("select i from Inscripcion i "
            + "join fetch i.estudiante e "
            + "join fetch i.curso c "
            + "where c.fechaInicio is not null and c.fechaFin is not null "
            + "and c.fechaInicio <= :periodoHasta and c.fechaFin >= :periodoDesde "
            + "and (:programa is null or lower(trim(e.programaAcademico)) = lower(:programa)) "
            + "and (:cursoId is null or c.id = :cursoId) "
            + "order by e.apellido asc, e.nombre asc, c.nombre asc")
    List<Inscripcion> buscarMatriculasParaReporte(@Param("periodoDesde") LocalDate periodoDesde,
            @Param("periodoHasta") LocalDate periodoHasta,
            @Param("programa") String programa,
            @Param("cursoId") Long cursoId);
}
