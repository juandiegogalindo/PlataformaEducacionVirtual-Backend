package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocenteRepository extends JpaRepository<Docente, Long> {
    List<Docente> findByActivoTrueOrderByApellidoAscNombreAsc();

    @Query(value = "select id from docentes where id = :id for update", nativeQuery = true)
    Optional<Long> bloquearFilaParaCursos(@Param("id") Long id);
}
