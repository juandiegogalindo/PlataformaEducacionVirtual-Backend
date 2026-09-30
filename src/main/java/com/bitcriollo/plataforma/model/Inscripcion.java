package com.bitcriollo.plataforma.model;

import com.bitcriollo.plataforma.model.enums.EstadoInscripcion;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// Un estudiante no puede tener más de una inscripción para el mismo curso.
@Entity
@Table(name = "inscripciones", uniqueConstraints = @UniqueConstraint(columnNames = { "estudiante_id", "curso_id" }))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // El estado inicial de una inscripción es ACTIVA.
    private EstadoInscripcion estado = EstadoInscripcion.ACTIVA;

    @CreationTimestamp
    @Column(name = "fecha_inscripcion", nullable = false, updatable = false)
    private LocalDateTime fechaInscripcion;

    @Column(name = "calificacion_final")
    private Double calificacionFinal;
}