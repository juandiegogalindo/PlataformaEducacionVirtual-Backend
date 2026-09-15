package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.RecursoBibliografico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecursoBibliograficoRepository extends JpaRepository<RecursoBibliografico, Long> {
    List<RecursoBibliografico> findByCursoId(Long cursoId);
}