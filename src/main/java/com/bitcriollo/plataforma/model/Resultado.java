package com.bitcriollo.plataforma.model;

import com.bitcriollo.plataforma.model.enums.EstadoResultado;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "resultados", uniqueConstraints = @UniqueConstraint(columnNames = { "evaluacion_id", "estudiante_id",
        "numero_intento" }))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Resultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluacion_id", nullable = false)
    private Evaluacion evaluacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(name = "numero_intento", nullable = false)
    private Integer numeroIntento = 1;

    private Double calificacion;

    @Column(columnDefinition = "TEXT")
    private String retroalimentacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoResultado estado = EstadoResultado.PENDIENTE;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;
}