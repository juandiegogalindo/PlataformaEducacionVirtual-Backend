package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.Resultado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultadoRepository extends JpaRepository<Resultado, Long> {
    List<Resultado> findByEstudianteId(Long estudianteId);

    List<Resultado> findByEvaluacionId(Long evaluacionId);

    List<Resultado> findByEvaluacionIdAndEstudianteId(Long evaluacionId, Long estudianteId);

    List<Resultado> findByEvaluacionCursoId(Long cursoId);
}
