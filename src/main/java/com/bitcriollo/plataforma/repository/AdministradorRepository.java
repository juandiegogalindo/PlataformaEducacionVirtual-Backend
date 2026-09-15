package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
}