package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    Optional<Estudiante> findByCodigoEstudiantil(String codigoEstudiantil);
}