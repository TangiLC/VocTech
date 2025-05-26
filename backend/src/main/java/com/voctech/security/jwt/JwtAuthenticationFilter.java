package com.voctech.security.jwt;

import com.voctech.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Autowired
  private JwtTokenProvider tokenProvider;

  @Autowired
  private UserDetailsServiceImpl userDetailsService;

  @Value("${jwt.header}")
  private String jwtHeader;

  private static final Logger logger = LoggerFactory.getLogger(
    JwtAuthenticationFilter.class
  );

  // Liste des chemins qui ne nécessitent pas d'authentification - alignés avec SecurityConfig
  private final List<String> PUBLIC_PATHS = Arrays.asList(
    "/auth/",
    "/swagger-ui/",
    "/v3/api-docs/",
    "/swagger-resources/",
    "/swagger-ui.html"
  );

  @SuppressWarnings("null")
  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    try {
      // Récupérer le JWT depuis l'en-tête
      String jwt = getJwtFromRequest(request);
      String path = request.getServletPath();

      logger.debug("Processing request for path: {}", path);
      logger.debug("JWT token present: {}", StringUtils.hasText(jwt));

      // Si le token est présent et valide, configurer l'authentification
      if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
        logger.debug("JWT token is valid");
        String username = tokenProvider.getUsernameFromToken(jwt);
        logger.debug("Username extracted from token: {}", username);

        UserDetails userDetails = userDetailsService.loadUserByUsername(
          username
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          userDetails,
          null,
          userDetails.getAuthorities()
        );
        authentication.setDetails(
          new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.debug(
          "Authentication set in SecurityContextHolder for user: {}",
          username
        );
      } else if (StringUtils.hasText(jwt)) {
        logger.warn("Invalid JWT token: {}", jwt);
      } else {
        logger.debug("No JWT token found in request");
      }
    } catch (Exception ex) {
      logger.error("Could not set user authentication in security context", ex);
      // Ne pas supprimer l'exception, laisser Spring Security gérer la réponse
    }

    filterChain.doFilter(request, response);
  }

  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader(jwtHeader);
    logger.debug("Raw Authorization header: {}", bearerToken);

    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }

    return null;
  }
}
