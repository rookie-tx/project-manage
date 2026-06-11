package cn.tx.aiagent.common.security;

import cn.tx.aiagent.login.domain.LoginContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String createToken(LoginContext ctx) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpirationDays() * 86400000L);

        return Jwts.builder()
                .subject(String.valueOf(ctx.userId()))
                .claim("username", ctx.username())
                .claim("name", ctx.name())
                .claim("roles", ctx.roles())
                .claim("permissions", ctx.permissions())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getKey())
                .compact();
    }

    public LoginContext parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long userId = Long.parseLong(claims.getSubject());
        String username = claims.get("username", String.class);
        String name = claims.get("name", String.class);
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        @SuppressWarnings("unchecked")
        List<String> permissions = claims.get("permissions", List.class);

        return new LoginContext(userId, username, name,
                roles != null ? roles : List.of(),
                permissions != null ? permissions : List.of(),
                false);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private SecretKey getKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        // jjwt requires at least 256 bits key for HS256
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
