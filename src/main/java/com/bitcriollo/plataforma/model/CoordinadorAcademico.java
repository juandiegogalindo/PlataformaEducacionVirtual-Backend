package com.bitcriollo.plataforma.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coordinadores_academicos")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CoordinadorAcademico extends Usuario {

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String facultad;

    @Column(name = "telefono_oficina")
    private String telefonoOficina;
}