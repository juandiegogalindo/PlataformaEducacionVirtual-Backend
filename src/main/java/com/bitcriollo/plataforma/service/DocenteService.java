package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.DocenteResumenResponse;
import com.bitcriollo.plataforma.repository.DocenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocenteService {

    private final DocenteRepository docenteRepository;

    public DocenteService(DocenteRepository docenteRepository) {
        this.docenteRepository = docenteRepository;
    }

    @Transactional(readOnly = true)
    public List<DocenteResumenResponse> listarDocentesActivos() {
        return docenteRepository.findByActivoTrueOrderByApellidoAscNombreAsc().stream()
                .map(d -> new DocenteResumenResponse(
                        d.getId(), d.getNombre(), d.getApellido(), d.getCorreo(), d.getEspecialidad()))
                .toList();
    }
}