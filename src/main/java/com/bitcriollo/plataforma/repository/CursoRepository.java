package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.Curso;
import com.bitcriollo.plataforma.model.enums.EstadoCurso;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    List<Curso> findByDocenteId(Long docenteId);

    List<Curso> findByEstado(EstadoCurso estado);

    // Bloquea la fila del curso hasta el fin de la transaccion para serializar las
    // inscripciones
    // concurrentes y evitar que se supere el cupo maximo (condicion de carrera).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Curso c where c.id = :id")
    Optional<Curso> findByIdParaInscripcion(Long id);

    // Cuenta los cursos ACTIVOS de un docente cuyas fechas se solapan con el rango
    // indicado.
    // cursoIdExcluir permite ignorar el propio curso al validar una actualizacion
    // (puede ser null al crear).
    @Query("select count(c) from Curso c where c.docente.id = :docenteId "
            + "and c.estado = com.bitcriollo.plataforma.model.enums.EstadoCurso.ACTIVO "
            + "and (:cursoIdExcluir is null or c.id <> :cursoIdExcluir) "
            + "and c.fechaInicio <= :fechaFin and c.fechaFin >= :fechaInicio")
    long countCursosActivosConCruce(@Param("docenteId") Long docenteId,
            @Param("cursoIdExcluir") Long cursoIdExcluir,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    // Un curso se considera "el mismo" si coincide exactamente en nombre, docente y
    // fechas.
    boolean existsByNombreAndDocenteIdAndFechaInicioAndFechaFinAndEstado(String nombre, Long docenteId,
            LocalDate fechaInicio, LocalDate fechaFin, EstadoCurso estado);
}