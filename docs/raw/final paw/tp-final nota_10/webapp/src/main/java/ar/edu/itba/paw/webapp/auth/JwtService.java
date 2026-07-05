package ar.edu.itba.paw.webapp.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final long ACCESS_TOKEN_EXPIRATION = 1 * 1000 * 60 * 15; // 15 minutos
    private final long REFRESH_TOKEN_EXPIRATION = 1 * 1000 * 60 * 60 * 24 * 7; // 7 días

    @Value("classpath:openssl/access.key")
    private Resource secretKeyResource;

    @Value("classpath:openssl/refresh.key")
    private Resource refreshKeyResource;

    private SecretKey accessKey;
    private SecretKey refreshKey;

    @PostConstruct
    private void initKeys() throws IOException {
        byte[] accessKeyBytes = Files.readAllBytes(secretKeyResource.getFile().toPath());
        this.accessKey = Keys.hmacShaKeyFor(accessKeyBytes);

        byte[] refreshKeyBytes = Files.readAllBytes(refreshKeyResource.getFile().toPath());
        this.refreshKey = Keys.hmacShaKeyFor(refreshKeyBytes);
    }

    private Claims verifyToken(String token, SecretKey key) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        if (claims.getExpiration() != null && claims.getExpiration().before(new Date())) {
            throw new ExpiredJwtException(null, claims, "JWT token expired");
        }

        return claims;
    }

    public Claims verifyAccessToken(String token) {
        return verifyToken(token, accessKey);
    }

    public Claims verifyRefreshToken(String token) {
        return verifyToken(token, refreshKey);
    }

    public String generateAccessToken(AuthUser userDetails) {
        return generateToken(userDetails, new Date(), ACCESS_TOKEN_EXPIRATION, accessKey);
    }

    public String generateRefreshToken(AuthUser userDetails) {
        return generateToken(userDetails, new Date(), REFRESH_TOKEN_EXPIRATION, refreshKey);
    }

    private String generateToken(AuthUser userDetails, Date issuedAt, long expiration, SecretKey key) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        claims.put("userId", userDetails.getUserId());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(issuedAt)
                .setExpiration(new Date(issuedAt.getTime() + expiration))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

}
