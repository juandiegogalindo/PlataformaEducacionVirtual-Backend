package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocenteRepository extends JpaRepository<Docente, Long> {
}