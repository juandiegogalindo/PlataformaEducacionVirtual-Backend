package com.bitcriollo.plataforma.repository;

import com.bitcriollo.plataforma.model.MensajeForo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeForoRepository extends JpaRepository<MensajeForo, Long> {
    List<MensajeForo> findByForoIdAndMensajePadreIsNullOrderByFechaPublicacionAsc(Long foroId);

    List<MensajeForo> findByMensajePadreId(Long mensajePadreId);
}