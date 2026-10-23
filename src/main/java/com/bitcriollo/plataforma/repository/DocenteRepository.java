package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocenteRepository extends JpaRepository<Docente, Long> {
    List<Docente> findByActivoTrueOrderByApellidoAscNombreAsc();
}