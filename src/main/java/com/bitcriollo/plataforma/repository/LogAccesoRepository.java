package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.LogAcceso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogAccesoRepository extends JpaRepository<LogAcceso, Long> {
}