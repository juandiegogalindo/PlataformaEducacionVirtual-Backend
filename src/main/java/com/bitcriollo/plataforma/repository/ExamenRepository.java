package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.Examen;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamenRepository extends JpaRepository<Examen, Long> {
}