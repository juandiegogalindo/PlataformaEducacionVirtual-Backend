package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.AdministradorRequest;
import com.bitcriollo.plataforma.dto.CoordinadorRequest;
import com.bitcriollo.plataforma.dto.DocenteRequest;
import com.bitcriollo.plataforma.dto.UsuarioResponse;
import com.bitcriollo.plataforma.model.Administrador;
import com.bitcriollo.plataforma.model.CoordinadorAcademico;
import com.bitcriollo.plataforma.model.Docente;
import com.bitcriollo.plataforma.model.Usuario;
import com.bitcriollo.plataforma.model.enums.NivelAccesoAdmin;
import com.bitcriollo.plataforma.repository.AdministradorRepository;
import com.bitcriollo.plataforma.repository.CoordinadorAcademicoRepository;
import com.bitcriollo.plataforma.repository.DocenteRepository;
import com.bitcriollo.plataforma.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bitcriollo.plataforma.repository.EstudianteRepository;
import java.util.List;

@Service
public class AdminUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final DocenteRepository docenteRepository;
    private final AdministradorRepository administradorRepository;
    private final CoordinadorAcademicoRepository coordinadorAcademicoRepository;
    private final EstudianteRepository estudianteRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUsuarioService(UsuarioRepository usuarioRepository,
            DocenteRepository docenteRepository,
            AdministradorRepository administradorRepository,
            CoordinadorAcademicoRepository coordinadorAcademicoRepository,
            EstudianteRepository estudianteRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.docenteRepository = docenteRepository;
        this.administradorRepository = administradorRepository;
        this.coordinadorAcademicoRepository = coordinadorAcademicoRepository;
        this.estudianteRepository = estudianteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse crearDocente(DocenteRequest request) {
        // Se valida que el correo no esté registrado antes de crear el usuario.
        validarCorreoDisponible(request.getCorreo());

        Docente docente = new Docente();
        docente.setNombre(request.getNombre());
        docente.setApellido(request.getApellido());
        docente.setCorreo(request.getCorreo());
        docente.setContrasenaHash(passwordEncoder.encode(request.getContrasena()));
        docente.setCodigoDocente(request.getCodigoDocente());
        docente.setEspecialidad(request.getEspecialidad());
        docente.setTituloProfesional(request.getTituloProfesional());
        docente.setAniosExperiencia(request.getAniosExperiencia());
        docente.setBiografia(request.getBiografia());

        docenteRepository.save(docente);
        return mapearAResponse(docente);
    }

    @Transactional
    public UsuarioResponse crearAdministrador(AdministradorRequest request) {
        // Se valida que el correo no esté registrado antes de crear el usuario.
        validarCorreoDisponible(request.getCorreo());

        Administrador administrador = new Administrador();
        administrador.setNombre(request.getNombre());
        administrador.setApellido(request.getApellido());
        administrador.setCorreo(request.getCorreo());
        administrador.setContrasenaHash(passwordEncoder.encode(request.getContrasena()));
        administrador.setCodigo(request.getCodigo());
        administrador.setCargo(request.getCargo());
        administrador.setNivelAcceso(
                request.getNivelAcceso() != null ? request.getNivelAcceso() : NivelAccesoAdmin.ADMIN);

        administradorRepository.save(administrador);
        return mapearAResponse(administrador);
    }

    @Transactional
    public UsuarioResponse crearCoordinador(CoordinadorRequest request) {
        // Se valida que el correo no esté registrado antes de crear el usuario.
        validarCorreoDisponible(request.getCorreo());

        CoordinadorAcademico coordinador = new CoordinadorAcademico();
        coordinador.setNombre(request.getNombre());
        coordinador.setApellido(request.getApellido());
        coordinador.setCorreo(request.getCorreo());
        coordinador.setContrasenaHash(passwordEncoder.encode(request.getContrasena()));
        coordinador.setCodigo(request.getCodigo());
        coordinador.setFacultad(request.getFacultad());
        coordinador.setTelefonoOficina(request.getTelefonoOficina());

        coordinadorAcademicoRepository.save(coordinador);
        return mapearAResponse(coordinador);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarDocentes() {
        return docenteRepository.findAll().stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarAdministradores() {
        return administradorRepository.findAll().stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarCoordinadores() {
        return coordinadorAcademicoRepository.findAll().stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarEstudiantes() {
        return estudianteRepository.findAll().stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Transactional
    public UsuarioResponse desactivarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un usuario con id " + id));
        // Desactivar el usuario impide su acceso sin eliminar sus datos del sistema.
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        return mapearAResponse(usuario);
    }

    @Transactional
    public UsuarioResponse activarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un usuario con id " + id));
        // Activar el usuario permite nuevamente su acceso al sistema.
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
        return mapearAResponse(usuario);
    }

    private void validarCorreoDisponible(String correo) {
        // El correo se utiliza como identificador único de autenticación.
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese correo");
        }
    }

    private UsuarioResponse mapearAResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getClass().getSimpleName(),
                usuario.isActivo(),
                usuario.getCreatedAt(),
                usuario.getTelefono(),
                usuario.getFotoPerfilUrl());
    }
}