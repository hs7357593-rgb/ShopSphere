package com.vendora;
import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets; import java.util.Date; import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
@Service
public class JwtService {
  private final SecretKey key; private final long expiryMs;
  public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiry-minutes}") long minutes){
    if(secret==null||secret.getBytes(StandardCharsets.UTF_8).length<32) throw new IllegalStateException("JWT_SECRET must be set to at least 32 characters");
    key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); expiryMs=minutes*60_000L; }
  public String issue(AppUser u){
    return Jwts.builder().subject(String.valueOf(u.id)).claim("email",u.email).claim("role",u.role.name())
      .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+expiryMs)).signWith(key).compact(); }
  public Long userId(String token){
    Claims c=Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); return Long.valueOf(c.getSubject()); }
}
