package com.bitcriollo.plataforma.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "progreso_lecciones", uniqueConstraints = @UniqueConstraint(columnNames = { "estudiante_id",
        "leccion_id" }))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class ProgresoLeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leccion_id", nullable = false)
    private Leccion leccion;

    @Column(nullable = false)
    private boolean completado = false;

    @Column(name = "fecha_completado")
    private LocalDateTime fechaCompletado;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}