package com.bitcriollo.plataforma.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "recursos_bibliograficos")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class RecursoBibliografico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column(nullable = false)
    private String titulo;

    private String autor;

    @Column(name = "anio_publicacion")
    private Integer anioPublicacion;

    @Column(name = "tipo_recurso", nullable = false)
    private String tipoRecurso;

    @Column(nullable = false)
    private String enlace;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}