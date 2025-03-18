package com.voctech.security.jwt;

import com.voctech.security.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private static final Logger logger = LoggerFactory.getLogger(
    JwtTokenProvider.class
  );

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.expiration}")
  private int jwtExpirationMs;

  @Value("${jwt.header}")
  private String jwtHeader;

  @Value("${jwt.token.prefix}")
  private String jwtTokenPrefix;

  public String generateToken(Authentication authentication) {
    UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

    SecretKey key = Keys.hmacShaKeyFor(
      jwtSecret.getBytes(StandardCharsets.UTF_8)
    );

    return Jwts
      .builder()
      .setSubject(userPrincipal.getUsername())
      .claim("roles", userPrincipal.getAuthorities())
      .setIssuedAt(now)
      .setExpiration(expiryDate)
      .signWith(key)
      .compact();
  }

  public String getUsernameFromToken(String token) {
    SecretKey key = Keys.hmacShaKeyFor(
      jwtSecret.getBytes(StandardCharsets.UTF_8)
    );

    Claims claims = Jwts
      .parserBuilder()
      .setSigningKey(key)
      .build()
      .parseClaimsJws(token)
      .getBody();

    return claims.getSubject();
  }

  public boolean validateToken(String authToken) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(
        jwtSecret.getBytes(StandardCharsets.UTF_8)
      );
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
      return true;
    } catch (SignatureException ex) {
      logger.error("Invalid JWT signature");
    } catch (MalformedJwtException ex) {
      logger.error("Invalid JWT token");
    } catch (ExpiredJwtException ex) {
      logger.error("Expired JWT token");
    } catch (UnsupportedJwtException ex) {
      logger.error("Unsupported JWT token");
    } catch (IllegalArgumentException ex) {
      logger.error("JWT claims string is empty");
    }
    return false;
  }

  public String getJwtFromHeader(String headerValue) {
    if (headerValue != null && headerValue.startsWith(jwtTokenPrefix)) {
      return headerValue.substring(jwtTokenPrefix.length() + 1);
    }
    return null;
  }
}
