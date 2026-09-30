package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TareaRepository extends JpaRepository<Tarea, Long> {
}