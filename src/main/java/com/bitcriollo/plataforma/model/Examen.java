package com.bitcriollo.plataforma.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "examenes")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Examen extends Evaluacion {

    @Column(name = "fecha_aplicacion", nullable = false)
    private LocalDateTime fechaAplicacion;

    @Column(name = "tiempo_limite", nullable = false)
    private Integer tiempoLimite;

    @Column(name = "numero_intentos_permitidos", nullable = false)
    private Integer numeroIntentosPermitidos = 1;

    @Column(nullable = false)
    private boolean aleatorio = false;
}