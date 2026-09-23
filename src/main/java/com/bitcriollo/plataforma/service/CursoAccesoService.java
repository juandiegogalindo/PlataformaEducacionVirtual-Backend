package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.model.Curso;
import com.bitcriollo.plataforma.model.Estudiante;
import com.bitcriollo.plataforma.model.Usuario;
import com.bitcriollo.plataforma.model.enums.EstadoInscripcion;
import com.bitcriollo.plataforma.repository.InscripcionRepository;
import org.springframework.stereotype.Service;

// Componente transversal reutilizado por los servicios de Contenidos, Evaluacion e Interaccion para
// evitar reimplementar en cada uno la comprobacion de si un usuario dicta un curso o si un estudiante
// tiene una inscripcion activa en el (estandar 2.1.5 del Plan de Implementacion).
@Service
public class CursoAccesoService {

    private final InscripcionRepository inscripcionRepository;

    public CursoAccesoService(InscripcionRepository inscripcionRepository) {
        this.inscripcionRepository = inscripcionRepository;
    }

    public boolean esDocenteDelCurso(Curso curso, Usuario usuario) {
        return curso.getDocente().getId().equals(usuario.getId());
    }

    // Un estudiante solo tiene acceso al contenido de un curso mientras su inscripcion este ACTIVA.
    public boolean estaInscritoActivo(Curso curso, Usuario usuario) {
        return usuario instanceof Estudiante
                && inscripcionRepository.findByEstudianteIdAndCursoId(usuario.getId(), curso.getId())
                        .filter(i -> i.getEstado() == EstadoInscripcion.ACTIVA)
                        .isPresent();
    }
}
