package com.voctech.controller;

import com.voctech.model.User;
import com.voctech.payload.JwtResponse;
import com.voctech.payload.LoginRequest;
import com.voctech.payload.MessageResponse;
import com.voctech.payload.SignupRequest;
import com.voctech.repository.UserRepository;
import com.voctech.security.jwt.JwtTokenProvider;
import com.voctech.security.service.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur d'authentification
 * permettant aux utilisateurs de s'inscrire et de se connecter.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(
  name = "Authentification",
  description = "Endpoints pour l'authentification et l'inscription des utilisateurs"
)
public class AuthController {

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder encoder;

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  /**
   * Authentifie un utilisateur et génère un jeton JWT.
   *
   * @param loginRequest Requête contenant le nom d'utilisateur et le mot de passe.
   * @return Une réponse contenant le jeton JWT et les détails de l'utilisateur.
   */
  @PostMapping("/login")
  @Operation(
    summary = "Authentification de l'utilisateur",
    description = "Permet à un utilisateur de se connecter et d'obtenir un jeton JWT.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "Authentification réussie",
        content = @Content(schema = @Schema(implementation = JwtResponse.class))
      ),
      @ApiResponse(
        responseCode = "401",
        description = "Identifiants invalides"
      ),
    }
  )
  public ResponseEntity<?> authenticateUser(
    @Valid @RequestBody LoginRequest loginRequest
  ) {
    Authentication authentication = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(
        loginRequest.getUsername(),
        loginRequest.getPassword()
      )
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtTokenProvider.generateToken(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    String role = userDetails.getAuthorities().stream()
    .map(GrantedAuthority::getAuthority)
    .findFirst()
    .orElse(null);

    return ResponseEntity.ok(
      new JwtResponse(
        jwt,
        userDetails.getId(),
        userDetails.getUsername(),
        userDetails.getEmail(),
        role
      )
    );
  }

  /**
   * Enregistre un nouvel utilisateur dans la base de données.
   *
   * @param signUpRequest Requête contenant les informations de l'utilisateur.
   * @return Une réponse indiquant si l'inscription a réussi.
   */
  @PostMapping("/register")
  @Operation(
    summary = "Inscription d'un utilisateur",
    description = "Permet à un nouvel utilisateur de s'inscrire avec un rôle spécifique.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "Utilisateur enregistré avec succès",
        content = @Content(
          schema = @Schema(implementation = MessageResponse.class)
        )
      ),
      @ApiResponse(
        responseCode = "400",
        description = "Erreur lors de l'inscription (nom d'utilisateur ou email déjà utilisé)"
      ),
    }
  )
  public ResponseEntity<?> registerUser(
    @Valid @RequestBody SignupRequest signUpRequest
  ) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity
        .badRequest()
        .body(new MessageResponse("Erreur: Nom d'utilisateur déjà existant!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity
        .badRequest()
        .body(new MessageResponse("Erreur: Email déjà utilisé!"));
    }

    // Créer l'utilisateur avec le constructeur existant
    User user = new User(
      signUpRequest.getUsername(),
      signUpRequest.getEmail(),
      encoder.encode(signUpRequest.getPassword())
    );

    // Déterminer le rôle basé sur la requête
    String requestedRole = null;
    /*if (
      signUpRequest.getRole() != null && !signUpRequest.getRole().isEmpty()
    ) {
      requestedRole = signUpRequest.getRole().iterator().next();
    }*/

    // Assigner le rôle (par défaut USER)
    String roleString = "admin".equalsIgnoreCase(requestedRole)
      ? "ADMIN"
      : "USER";
    user.setRole(roleString);

    userRepository.save(user);

    return ResponseEntity.ok(
      new MessageResponse("Utilisateur enregistré avec succès!")
    );
  }
}
