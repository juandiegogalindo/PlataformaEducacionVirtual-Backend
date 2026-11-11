package com.bitcriollo.plataforma;

import com.bitcriollo.plataforma.dto.*;
import com.bitcriollo.plataforma.exception.CredencialesInvalidasException;
import com.bitcriollo.plataforma.model.*;
import com.bitcriollo.plataforma.model.enums.*;
import com.bitcriollo.plataforma.repository.*;
import com.bitcriollo.plataforma.security.JwtService;
import com.bitcriollo.plataforma.security.UsuarioDetailsImpl;
import com.bitcriollo.plataforma.service.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:revision;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false",
        "app.auth.refresh-cookie.enabled=false",
        "app.jwt.secret=clave-exclusiva-de-pruebas-con-mas-de-32-bytes",
        "logging.level.org.hibernate.SQL=OFF"
})
@AutoConfigureMockMvc
class RevisionIntegracionTest {
    @Autowired private AuthService auth;
    @Autowired private AuthAuditService auditoria;
    @Autowired private EvaluacionService evaluacionesService;
    @Autowired private ResultadoService resultadosService;
    @Autowired private EstudianteRepository estudiantes;
    @Autowired private DocenteRepository docentes;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private CursoRepository cursos;
    @Autowired private RefreshTokenRepository tokens;
    @Autowired private EvaluacionRepository evaluaciones;
    @Autowired private TareaRepository tareas;
    @Autowired private InscripcionRepository inscripciones;
    @Autowired private LeccionRepository lecciones;
    @Autowired private ProgresoLeccionRepository progresos;
    @Autowired private PasswordEncoder encoder;
    @Autowired private TransactionTemplate tx;
    @Autowired private EntityManager em;
    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwt;

    @Test
    void dosRenovacionesSimultaneasSoloCreanUnaSesion() throws Exception {
        Estudiante estudiante = estudiante();
        RefreshToken token = token(estudiante);
        List<String> resultados = enParalelo(2, () -> {
            try {
                return auth.refrescarToken(refresh(token.getToken())).getRol();
            } catch (CredencialesInvalidasException ex) {
                return "RECHAZADO";
            }
        });
        assertThat(resultados).containsExactlyInAnyOrder("Estudiante", "RECHAZADO");
        assertThat(tokens.findById(token.getId()).orElseThrow().isRevocado()).isTrue();
        assertThat(tokens.findByUsuarioIdAndRevocadoFalse(estudiante.getId())).hasSize(1);
    }

    @Test
    void dosEvaluacionesSimultaneasNoSuperanElCienPorCiento() throws Exception {
        Docente docente = docente();
        Curso curso = curso(docente);
        List<String> resultados = enParalelo(2, () -> {
            try {
                evaluacionesService.crearTarea(curso.getId(), tareaRequest(60.0), docente);
                return "CREADA";
            } catch (IllegalStateException ex) {
                return "RECHAZADA";
            }
        });
        assertThat(resultados).containsExactlyInAnyOrder("CREADA", "RECHAZADA");
        assertThat(evaluaciones.findByCursoId(curso.getId())).hasSize(1);
        assertThat(evaluaciones.findByCursoId(curso.getId()).get(0).getPesoPorcentual()).isEqualTo(60.0);
    }

    @Test
    void tresFallosConcurrentesSeConservanYBloqueanLaCuenta() throws Exception {
        Estudiante estudiante = estudiante();
        enParalelo(3, () -> {
            auditoria.registrarIntentoFallido(estudiante);
            return "REGISTRADO";
        });
        Usuario actual = usuarios.findById(estudiante.getId()).orElseThrow();
        assertThat(actual.getIntentosFallidos()).isEqualTo(3);
        assertThat(actual.getBloqueadoHasta()).isAfter(LocalDateTime.now());
    }

    @Test
    void elFalloDeLoginNoRevierteElContadorNiElLog() {
        Estudiante estudiante = estudiante();
        LoginRequest request = new LoginRequest();
        request.setCorreo(estudiante.getCorreo());
        request.setContrasena("incorrecta");
        assertThatThrownBy(() -> auth.login(request, "127.0.0.1")).isInstanceOf(CredencialesInvalidasException.class);
        assertThat(usuarios.findById(estudiante.getId()).orElseThrow().getIntentosFallidos()).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from log_accesos where usuario_id = ? and exitoso = false",
                Long.class, estudiante.getId())).isEqualTo(1L);
    }

    @Test
    void unProxyDeUsuarioConservaSuRolReal() {
        Estudiante estudiante = estudiante();
        tx.executeWithoutResult(status -> {
            Usuario proxy = em.getReference(Usuario.class, estudiante.getId());
            assertThat(proxy).isInstanceOf(org.hibernate.proxy.HibernateProxy.class);
            UsuarioDetailsImpl details = new UsuarioDetailsImpl(proxy);
            assertThat(details.getUsuario()).isInstanceOf(Estudiante.class);
            assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_ESTUDIANTE");
        });
    }

    @Test
    void refreshInvalidoODeCuentaDesactivadaResponde401() throws Exception {
        mvc.perform(post("/api/auth/refresh").contentType("application/json")
                .content("{\"refreshToken\":\"inexistente\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("status").value(401));
        Estudiante estudiante = estudiante();
        RefreshToken token = token(estudiante);
        estudiante.setActivo(false);
        estudiantes.saveAndFlush(estudiante);
        mvc.perform(post("/api/auth/refresh").contentType("application/json")
                .content("{\"refreshToken\":\"" + token.getToken() + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void distingueEntreRutaPublicaFaltaDeAutenticacionYFaltaDePermisos() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk());
        mvc.perform(get("/api/perfil")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/docentes").with(user(new UsuarioDetailsImpl(estudiante()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unHorarioNuloSeRechazaCon400() throws Exception {
        mvc.perform(post("/api/cursos").with(user(new UsuarioDetailsImpl(docente())))
                .contentType("application/json")
                .content("{\"nombre\":\"Curso\",\"fechaInicio\":\"2026-11-10\",\"fechaFin\":\"2026-12-10\",\"horarios\":[null]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void elFiltroJwtPermiteConsultarElPerfilConUnTokenValido() throws Exception {
        String accessToken = jwt.generarToken(new UsuarioDetailsImpl(estudiante()));
        mvc.perform(get("/api/perfil").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk()).andExpect(jsonPath("rol").value("Estudiante"));
    }

    @Test
    void unaInscripcionCompletadaPermiteLeerEvaluacionesPeroNoEntregar() {
        Estudiante estudiante = estudiante();
        Curso curso = curso(docente());
        tx.executeWithoutResult(status -> {
            Inscripcion inscripcion = new Inscripcion();
            inscripcion.setCurso(curso);
            inscripcion.setEstudiante(estudiante);
            inscripcion.setEstado(EstadoInscripcion.COMPLETADA);
            inscripciones.save(inscripcion);
            Tarea tarea = new Tarea();
            tarea.setCurso(curso);
            tarea.setTitulo("Publicada");
            tarea.setPesoPorcentual(40.0);
            tarea.setFechaLimite(LocalDateTime.now().plusDays(1));
            tarea.setEstado(EstadoEvaluacion.PUBLICADA);
            tareas.save(tarea);
        });
        assertThat(evaluacionesService.listarPorCurso(curso.getId(), estudiante)).hasSize(1);
        Long id = evaluaciones.findByCursoId(curso.getId()).get(0).getId();
        assertThatThrownBy(() -> resultadosService.registrarResultado(id, new RegistrarResultadoRequest(), estudiante))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void laConsultaDeProgresoExcluyeOtrosCursosDelMismoEstudiante() {
        Estudiante estudiante = estudiante();
        Docente docente = docente();
        Curso primero = curso(docente);
        Curso segundo = curso(docente);
        tx.executeWithoutResult(status -> {
            for (Curso curso : List.of(primero, segundo)) {
                Leccion leccion = new Leccion();
                leccion.setCurso(curso);
                leccion.setTitulo("Leccion");
                leccion.setOrden(1);
                lecciones.save(leccion);
                ProgresoLeccion progreso = new ProgresoLeccion();
                progreso.setEstudiante(estudiante);
                progreso.setLeccion(leccion);
                progreso.setCompletado(true);
                progresos.save(progreso);
            }
        });
        tx.executeWithoutResult(status ->
                assertThat(progresos.findByEstudianteIdAndLeccionCursoId(estudiante.getId(), primero.getId()))
                        .hasSize(1).allMatch(p -> p.getLeccion().getCurso().getId().equals(primero.getId())));
    }

    private Estudiante estudiante() {
        return tx.execute(status -> {
            String id = UUID.randomUUID().toString();
            Estudiante estudiante = new Estudiante();
            estudiante.setNombre("Estudiante");
            estudiante.setApellido("Prueba");
            estudiante.setCorreo(id + "@test.local");
            estudiante.setContrasenaHash(encoder.encode("clave-de-prueba"));
            estudiante.setCodigoEstudiantil(id);
            estudiante.setTipoDocumento(TipoDocumento.CEDULA);
            estudiante.setNumeroDocumento(id);
            estudiante.setProgramaAcademico("Ingenieria");
            estudiante.setSemestre(1);
            return estudiantes.saveAndFlush(estudiante);
        });
    }

    private Docente docente() {
        return tx.execute(status -> {
            String id = UUID.randomUUID().toString();
            Docente docente = new Docente();
            docente.setNombre("Docente");
            docente.setApellido("Prueba");
            docente.setCorreo(id + "@test.local");
            docente.setContrasenaHash("hash-de-prueba");
            docente.setCodigoDocente(id);
            docente.setEspecialidad("Software");
            docente.setTituloProfesional("Ingeniero");
            return docentes.saveAndFlush(docente);
        });
    }

    private Curso curso(Docente docente) {
        return tx.execute(status -> {
            Curso curso = new Curso();
            curso.setNombre("Curso " + UUID.randomUUID());
            curso.setDocente(docente);
            curso.setCreadoPor(docente);
            curso.setFechaInicio(LocalDate.now());
            curso.setFechaFin(LocalDate.now().plusMonths(1));
            return cursos.saveAndFlush(curso);
        });
    }

    private RefreshToken token(Estudiante estudiante) {
        return tx.execute(status -> {
            RefreshToken token = new RefreshToken();
            token.setUsuario(estudiante);
            token.setToken(UUID.randomUUID().toString());
            token.setFechaExpiracion(LocalDateTime.now().plusDays(1));
            return tokens.saveAndFlush(token);
        });
    }

    private RefreshTokenRequest refresh(String token) {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(token);
        return request;
    }

    private TareaRequest tareaRequest(double peso) {
        TareaRequest request = new TareaRequest();
        request.setTitulo("Tarea");
        request.setPesoPorcentual(peso);
        request.setFechaLimite(LocalDateTime.now().plusDays(1));
        return request;
    }

    private <T> List<T> enParalelo(int cantidad, Supplier<T> operacion) throws Exception {
        ExecutorService ejecutor = Executors.newFixedThreadPool(cantidad);
        CountDownLatch listos = new CountDownLatch(cantidad);
        CountDownLatch inicio = new CountDownLatch(1);
        List<Future<T>> pendientes = new ArrayList<>();
        try {
            for (int i = 0; i < cantidad; i++) {
                pendientes.add(ejecutor.submit(() -> {
                    listos.countDown();
                    if (!inicio.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("No se inicio la prueba concurrente");
                    }
                    return operacion.get();
                }));
            }
            assertThat(listos.await(10, TimeUnit.SECONDS)).isTrue();
            inicio.countDown();
            List<T> resultados = new ArrayList<>();
            for (Future<T> pendiente : pendientes) {
                resultados.add(pendiente.get(20, TimeUnit.SECONDS));
            }
            return resultados;
        } finally {
            inicio.countDown();
            ejecutor.shutdownNow();
        }
    }
}
