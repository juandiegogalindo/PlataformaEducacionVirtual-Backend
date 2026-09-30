package com.bitcriollo.plataforma.model;

import com.bitcriollo.plataforma.model.enums.EstadoCurso;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cursos")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // El estado inicial del curso es ACTIVO al momento de crearlo.
    private EstadoCurso estado = EstadoCurso.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por_usuario_id", nullable = false)
    private Usuario creadoPor;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "cupo_maximo")
    private Integer cupoMaximo;

    @Column(name = "imagen_portada_url")
    private String imagenPortadaUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    // Las inscripciones pertenecen al curso y se eliminan cuando el curso deja de existir.
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    // Las lecciones pertenecen al curso y se eliminan cuando el curso deja de existir.
    private List<Leccion> lecciones = new ArrayList<>();

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    // Los recursos bibliográficos pertenecen al curso y se eliminan junto con él.
    private List<RecursoBibliografico> recursosBibliograficos = new ArrayList<>();

    @OneToOne(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    // El foro mantiene una relación uno a uno con el curso y depende de su ciclo de vida.
    private Foro foro;

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    // Las evaluaciones pertenecen al curso y se eliminan junto con él.
    private List<Evaluacion> evaluaciones = new ArrayList<>();
}