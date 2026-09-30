package com.bitcriollo.plataforma.model;

import com.bitcriollo.plataforma.model.enums.TipoDocumento;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "estudiantes")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Estudiante extends Usuario {

    @Column(name = "codigo_estudiantil", nullable = false, unique = true)
    private String codigoEstudiantil;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento", nullable = false, unique = true)
    private String numeroDocumento;

    @Column(name = "programa_academico", nullable = false)
    private String programaAcademico;

    @Column(nullable = false)
    private Integer semestre;
}