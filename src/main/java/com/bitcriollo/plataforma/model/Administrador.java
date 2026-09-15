package com.bitcriollo.plataforma.model;

import com.bitcriollo.plataforma.model.enums.NivelAccesoAdmin;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "administradores")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Administrador extends Usuario {

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String cargo;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_acceso", nullable = false)
    private NivelAccesoAdmin nivelAcceso = NivelAccesoAdmin.ADMIN;
}