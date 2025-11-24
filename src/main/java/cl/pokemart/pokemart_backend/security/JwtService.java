package cl.pokemart.pokemart_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final String secret;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-ms}") long expirationMs,
            @Value("${security.jwt.refresh-expiration-ms}") long refreshExpirationMs
    ) {
        this.secret = secret;
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateAccessToken(UserDetails userDetails, Map<String, Object> extraClaims) {
        return buildToken(extraClaims, userDetails, expirationMs);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(Map.of("type", "refresh"), userDetails, refreshExpirationMs);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        Date now = new Date(System.currentTimeMillis());
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
