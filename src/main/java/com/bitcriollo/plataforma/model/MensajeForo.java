package com.bitcriollo.plataforma.model;

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
@Table(name = "mensajes_foro")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class MensajeForo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "foro_id", nullable = false)
    private Foro foro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mensaje_padre_id")
    // Un mensaje puede ser una respuesta a otro mensaje del mismo foro.
    private MensajeForo mensajePadre;

    @OneToMany(mappedBy = "mensajePadre", cascade = CascadeType.ALL, orphanRemoval = true)
    // Las respuestas pertenecen al mensaje padre y se eliminan junto con él.
    private List<MensajeForo> respuestas = new ArrayList<>();

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenido;

    @Column(nullable = false)
    // Indica si el contenido del mensaje fue modificado después de su publicación.
    private boolean editado = false;

    @CreationTimestamp
    @Column(name = "fecha_publicacion", nullable = false, updatable = false)
    private LocalDateTime fechaPublicacion;
}