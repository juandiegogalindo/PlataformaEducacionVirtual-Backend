package com.bitcriollo.plataforma.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tareas")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Tarea extends Evaluacion {

    @Column(columnDefinition = "TEXT")
    private String instrucciones;

    @Column(name = "fecha_limite", nullable = false)
    private LocalDateTime fechaLimite;

    @Column(name = "permite_entrega_tardia", nullable = false)
    private boolean permiteEntregaTardia = false;

    @Column(name = "penalizacion_tardanza_porcentaje")
    private Double penalizacionTardanzaPorcentaje;
}