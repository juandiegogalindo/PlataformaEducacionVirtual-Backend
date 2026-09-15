package com.bitcriollo.plataforma.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_accesos")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class LogAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "correo_intento", nullable = false)
    private String correoIntento;

    @Column(nullable = false)
    private boolean exitoso;

    @Column(name = "ip_origen")
    private String ipOrigen;

    @CreationTimestamp
    @Column(name = "fecha_intento", nullable = false, updatable = false)
    private LocalDateTime fechaIntento;
}