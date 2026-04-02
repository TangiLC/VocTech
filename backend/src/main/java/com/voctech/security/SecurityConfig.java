package com.voctech.security;

import com.voctech.security.jwt.JwtAccessDeniedHandler;
import com.voctech.security.jwt.JwtAuthenticationEntryPoint;
import com.voctech.security.jwt.JwtAuthenticationFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Active les annotations @PreAuthorize, @PostAuthorize, etc.
public class SecurityConfig {

  @Value("${cors.allowed-origins:http://localhost:4200}")
  private List<String> allowedOrigins;

  @Value("${cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
  private List<String> allowedMethods;

  @Value("${cors.allowed-headers:Authorization,Content-Type,X-Auth-Token}")
  private List<String> allowedHeaders;

  private final JwtAuthenticationEntryPoint unauthorizedHandler;
  private final JwtAccessDeniedHandler accessDeniedHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfig(
    JwtAuthenticationEntryPoint unauthorizedHandler,
    JwtAccessDeniedHandler accessDeniedHandler,
    JwtAuthenticationFilter jwtAuthenticationFilter
  ) {
    this.unauthorizedHandler = unauthorizedHandler;
    this.accessDeniedHandler = accessDeniedHandler;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
    AuthenticationConfiguration authenticationConfiguration
  ) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
    throws Exception {
    http
      .csrf(csrf -> csrf.disable()) // Désactiver CSRF car JWT est utilisé
      .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Configuration CORS
      .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      ) // JWT est stateless
      .exceptionHandling(exceptions ->
        exceptions
          .authenticationEntryPoint(unauthorizedHandler)
          .accessDeniedHandler(accessDeniedHandler)
      )
      .authorizeHttpRequests(auth ->
        auth
          .requestMatchers(
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/actuator/health"
          )
          .permitAll()
          .requestMatchers("/api/auth/**")
          .permitAll()
          .requestMatchers("/api/database/**")
          .hasRole("ADMIN")
          .anyRequest()
          .authenticated()
      )
      .addFilterBefore(
        jwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter.class
      );

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("http://localhost:4200","https://patrick.le-cadre.net")); // Accepter tous les domaines (ajuster si nécessaire)
    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedMethods(allowedMethods);
    configuration.setAllowedHeaders(allowedHeaders);
    configuration.setExposedHeaders(List.of("X-Auth-Token"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
