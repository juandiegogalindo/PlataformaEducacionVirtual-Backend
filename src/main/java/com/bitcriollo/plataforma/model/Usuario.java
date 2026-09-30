package com.bitcriollo.plataforma.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    // El correo debe ser único para identificar de forma inequívoca a cada usuario.
    private String correo;

    @Column(name = "contrasena_hash", nullable = false)
    private String contrasenaHash;

    private String telefono;

    @Column(name = "foto_perfil_url")
    private String fotoPerfilUrl;

    @Column(nullable = false)
    // El estado activo permite habilitar o deshabilitar el acceso del usuario.
    private boolean activo = true;

    @Column(name = "email_verificado", nullable = false)
    private boolean emailVerificado = false;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "intentos_fallidos", nullable = false)
    // Los intentos fallidos se utilizan para controlar posibles bloqueos de acceso.
    private int intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    // La fecha indica hasta cuándo permanece bloqueado el usuario, cuando aplica.
    private LocalDateTime bloqueadoHasta;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}