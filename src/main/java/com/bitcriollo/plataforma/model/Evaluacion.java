package com.bitcriollo.plataforma.model;

import com.bitcriollo.plataforma.model.enums.EstadoEvaluacion;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evaluaciones")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public abstract class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    // Una evaluación pertenece a un curso específico y se carga de forma diferida.
    private Curso curso;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "peso_porcentual", nullable = false)
    private Double pesoPorcentual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // El estado inicial de toda evaluación es BORRADOR hasta que sea publicada.
    private EstadoEvaluacion estado = EstadoEvaluacion.BORRADOR;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "evaluacion", cascade = CascadeType.ALL, orphanRemoval = true)
    // Los resultados pertenecen a la evaluación y se eliminan junto con ella.
    private List<Resultado> resultados = new ArrayList<>();
}