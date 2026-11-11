package com.bitcriollo.plataforma.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Transient
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
        if (token != null) {
            try {
                byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                        .digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                StringBuilder hash = new StringBuilder(64);
                for (byte b : digest) {
                    hash.append(String.format("%02x", b));
                }
                this.tokenHash = hash.toString();
            } catch (java.security.NoSuchAlgorithmException e) {
                throw new IllegalStateException("No se pudo proteger la sesion");
            }
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    // Un token revocado ya no puede actualizarse para renovar la autenticación.
    private boolean revocado = false;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "session_id", length = 36)
    private String sessionId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}