package disciplina.lip.AuxiliarRotinaEstudo.service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtAuthService {
    
    @Value("${spring.security.jwt.secret-key:${jwt.secret-key}}")
    private String secretKey;
    
    @Value("${spring.security.jwt.expiration-time:${jwt.expiration-time}}")
    private Long jwtExpiration;
    
    @Value("${spring.security.jwt.refresh-expiration-time:604800000}")
    private Long refreshTokenExpiration; 
    
    private Key getSignInKey(){
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        }
    }

    public Claims extractAllClaims(String token){
        return Jwts
            .parserBuilder()
            .setSigningKey(getSignInKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public Long getExpirationTime(){
        return jwtExpiration;
    }

    public String generateToken(UserDetails user){
        return generateToken(new HashMap<>(), user, jwtExpiration);
    }
    
    public String generateRefreshToken(UserDetails user){
        return generateToken(new HashMap<>(), user, refreshTokenExpiration);
    }
    
    private String generateToken(Map<String, Object> extraClaims, UserDetails user, Long expiration){
        return Jwts 
            .builder()
            .setClaims(extraClaims)
            .setSubject(user.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token){
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true; 
        }
    }

    public boolean isTokenAboutToExpire(String token){
        try {
            Date expiration = extractExpiration(token);
            Date now = new Date();
            long timeUntilExpiration = expiration.getTime() - now.getTime();
            return timeUntilExpiration < 300000; 
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isValidToken(String token, UserDetails user){
        try {
            final String email = extractUsername(token);
            return (email.equals(user.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String refreshToken(String refreshToken, UserDetails userDetails){
        if (isValidToken(refreshToken, userDetails)) {
            return generateToken(userDetails);
        }
        throw new RuntimeException("Refresh token inválido ou expirado");
    }
}