package com.voctech.security.jwt;

import com.voctech.security.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
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

    Map<String, Object> claims = new HashMap<>();
    claims.put("id", userPrincipal.getId());
    claims.put("username", userPrincipal.getUsername());
    claims.put("role", userPrincipal.getRole());

    SecretKey key = Keys.hmacShaKeyFor(
      jwtSecret.getBytes(StandardCharsets.UTF_8)
    );

    return Jwts
      .builder()
      .setSubject(userPrincipal.getUsername())
      .setClaims(claims)
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

    String username = claims.getSubject();
    if (username == null) {
      username = claims.get("username", String.class);
    }
    return username;
  }

  public boolean validateToken(String authToken) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(
        jwtSecret.getBytes(StandardCharsets.UTF_8)
      );
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
      return true;
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

  public Authentication getAuthentication(String token) {
    Claims claims = Jwts
      .parser()
      .setSigningKey(jwtSecret)
      .parseClaimsJws(token)
      .getBody();

    Long id = Long.valueOf(claims.get("id").toString());
    String username = claims.get("username", String.class);
    String role = claims.get("role", String.class);
    GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
    List<GrantedAuthority> authorities = List.of(authority);

    UserDetails userDetails = org.springframework.security.core.userdetails.User
      .builder()
      .username(username)
      .password("")
      .authorities(authorities)
      .build();

    return new UsernamePasswordAuthenticationToken(
      userDetails,
      token,
      authorities
    );
  }
}
