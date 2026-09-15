package com.bitcriollo.plataforma.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "docentes")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Docente extends Usuario {

    @Column(name = "codigo_docente", nullable = false, unique = true)
    private String codigoDocente;

    @Column(nullable = false)
    private String especialidad;

    @Column(name = "titulo_profesional", nullable = false)
    private String tituloProfesional;

    @Column(name = "anios_experiencia")
    private Integer aniosExperiencia = 0;

    @Column(columnDefinition = "TEXT")
    private String biografia;
}