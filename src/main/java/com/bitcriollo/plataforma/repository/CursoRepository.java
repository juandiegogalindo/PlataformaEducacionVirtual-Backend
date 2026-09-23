package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.Curso;
import com.bitcriollo.plataforma.model.enums.EstadoCurso;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    List<Curso> findByDocenteId(Long docenteId);

    List<Curso> findByEstado(EstadoCurso estado);

    // Bloquea la fila del curso hasta el fin de la transaccion para serializar las inscripciones
    // concurrentes y evitar que se supere el cupo maximo (condicion de carrera).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Curso c where c.id = :id")
    Optional<Curso> findByIdParaInscripcion(Long id);
}