package com.bitcriollo.plataforma.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "fotos_perfil")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class FotoPerfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "tipo_contenido", nullable = false)
    private String tipoContenido;

    // Sin @Lob: en PostgreSQL se guarda como bytea y viaja con la fila.
    @Column(nullable = false)
    private byte[] contenido;

    @Column(name = "tamanio_bytes", nullable = false)
    private long tamanioBytes;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}