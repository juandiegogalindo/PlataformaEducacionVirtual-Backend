package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.Curso;
import com.bitcriollo.plataforma.model.enums.EstadoCurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    List<Curso> findByDocenteId(Long docenteId);

    List<Curso> findByEstado(EstadoCurso estado);
}