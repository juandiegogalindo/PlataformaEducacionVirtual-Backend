package com.bitcriollo.plataforma.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import java.util.Date;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.jwt.issuer:plataforma-educativa}")
    private String issuer = "plataforma-educativa";

    @Value("${app.jwt.audience:plataforma-frontend}")
    private String audience = "plataforma-frontend";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @PostConstruct
    public void validarConfiguracionAlIniciar() {
        validarConfiguracion();
    }

    public String generarToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("typ", "access")
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extraerCorreo(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public boolean esTokenValido(String token, UserDetails userDetails) {
        Claims claims = extraerClaims(token);
        String correo = claims.getSubject();
        return "access".equals(claims.get("typ", String.class))
                && correo != null
                && correo.equals(userDetails.getUsername())
                && issuer.equals(claims.getIssuer())
                && claims.getAudience().contains(audience)
                && !claims.getExpiration().before(new Date());
    }

    public void validarConfiguracion() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new io.jsonwebtoken.security.WeakKeyException("JWT_SECRET debe tener al menos 256 bits");
        }
        if (expirationMs <= 0) {
            throw new IllegalStateException("La expiracion JWT debe ser positiva");
        }
    }

    private <T> T extraerClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extraerClaims(token));
    }

    private Claims extraerClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).requireIssuer(issuer)
                .requireAudience(audience).build().parseSignedClaims(token).getPayload();
    }
}