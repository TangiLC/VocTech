package com.voctech.controller;

import com.voctech.model.ERole;
import com.voctech.model.Role;
import com.voctech.model.User;
import com.voctech.payload.JwtResponse;
import com.voctech.payload.LoginRequest;
import com.voctech.payload.MessageResponse;
import com.voctech.payload.SignupRequest;
import com.voctech.repository.RoleRepository;
import com.voctech.repository.UserRepository;
import com.voctech.security.jwt.JwtTokenProvider;
import com.voctech.security.service.UserDetailsImpl;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtTokenProvider jwtTokenProvider;

  @PostMapping("/login")
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
    List<String> roles = userDetails
      .getAuthorities()
      .stream()
      .map(item -> item.getAuthority())
      .collect(Collectors.toList());

    return ResponseEntity.ok(
      new JwtResponse(
        jwt,
        userDetails.getId(),
        userDetails.getUsername(),
        userDetails.getEmail(),
        roles
      )
    );
  }

  @PostMapping("/register")
  public ResponseEntity<?> registerUser(
    @Valid @RequestBody SignupRequest signUpRequest
  ) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity
        .badRequest()
        .body(new MessageResponse("Erreur: Nom d'utilisateur déjà pris!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity
        .badRequest()
        .body(new MessageResponse("Erreur: Email déjà utilisé!"));
    }

    // Create new user's account
    User user = new User(
      signUpRequest.getUsername(),
      signUpRequest.getEmail(),
      encoder.encode(signUpRequest.getPassword())
    );

    Set<String> strRoles = signUpRequest.getRoles();
    Set<Role> roles = new HashSet<>();

    if (strRoles == null) {
      Role userRole = roleRepository
        .findByName(ERole.USER)
        .orElseThrow(() ->
          new RuntimeException("Erreur: Role USER est introuvable.")
        );
      roles.add(userRole);
    } else {
      strRoles.forEach(role -> {
        switch (role) {
          case "admin":
            Role adminRole = roleRepository
              .findByName(ERole.ADMIN)
              .orElseThrow(() ->
                new RuntimeException("Erreur: Role ADMIN est introuvable.")
              );
            roles.add(adminRole);
            break;
          default:
            Role userRole = roleRepository
              .findByName(ERole.USER)
              .orElseThrow(() ->
                new RuntimeException("Erreur: Role USER est introuvable.")
              );
            roles.add(userRole);
        }
      });
    }

    user.setRoles(roles);
    userRepository.save(user);

    return ResponseEntity.ok(
      new MessageResponse("Utilisateur enregistré avec succès!")
    );
  }
}
