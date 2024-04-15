package com.ss871104.oauth2security.security.token;

import com.ss871104.oauth2security.security.handler.CustomAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${jwt.variables.refreshTokenSecret}")
    private String refreshTokenSecretKey;
    @Value("${jwt.variables.refreshTokenExpirationMs}")
    private int refreshTokenExpirationMs;

    public String generateToken(TokenData tokenData) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("provider", tokenData.getProvider());
        claims.put("roles", tokenData.getRoles());
        claims.put("device", tokenData.getDevice());
        claims.put("host", tokenData.getHost());
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(tokenData.getOAuth2Id())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new CustomAuthenticationException("Unauthorized: Token Expired");
        } catch (UnsupportedJwtException e) {
            throw new CustomAuthenticationException("Unauthorized: Token Unsupported");
        } catch (MalformedJwtException e) {
            throw new CustomAuthenticationException("Unauthorized: Wrong Token Format");
        } catch (SignatureException e) {
            throw new CustomAuthenticationException("Unauthorized: Wrong Token Signature");
        }
    }

    private Key getSignInKey() {
        byte[] keyBytes = new byte[0];
        keyBytes = Decoders.BASE64.decode(refreshTokenSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}