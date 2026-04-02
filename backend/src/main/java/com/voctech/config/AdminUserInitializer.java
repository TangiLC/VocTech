package com.voctech.config;

import com.voctech.model.User;
import com.voctech.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(
    AdminUserInitializer.class
  );

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${admin.credentials:}")
  private String adminCredentials;

  @Value("${guest.credentials:}")
  private String guestCredentials;

  public AdminUserInitializer(
    UserRepository userRepository,
    PasswordEncoder passwordEncoder
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(ApplicationArguments args) {
    bootstrapUser(adminCredentials, "ADMIN", "ADMIN_CREDENTIALS");
    bootstrapUser(guestCredentials, "GUEST", "GUEST_CREDENTIALS");
  }

  private void bootstrapUser(
    String credentials,
    String role,
    String propertyName
  ) {
    if (credentials == null || credentials.isBlank()) {
      logger.info("No {} provided, skipping {} bootstrap", propertyName, role);
      return;
    }

    String[] parts = credentials.split("\\|", 2);
    if (parts.length != 2) {
      logger.warn(
        "Invalid {} format, expected username|password",
        propertyName
      );
      return;
    }

    String username = parts[0].trim();
    String rawPassword = parts[1].trim();

    if (username.isBlank() || rawPassword.isBlank()) {
      logger.warn("{} contains empty username or password", propertyName);
      return;
    }

    userRepository
      .findByUsername(username)
      .ifPresentOrElse(
        existingUser -> ensureUserRole(existingUser, rawPassword, role),
        () -> createBootstrapUser(username, rawPassword, role)
      );
  }

  private void ensureUserRole(
    User existingUser,
    String rawPassword,
    String role
  ) {
    boolean updated = false;

    if (!role.equals(existingUser.getRole())) {
      existingUser.setRole(role);
      updated = true;
    }

    if (!passwordEncoder.matches(rawPassword, existingUser.getPassword())) {
      existingUser.setPassword(passwordEncoder.encode(rawPassword));
      updated = true;
    }

    if (updated) {
      userRepository.save(existingUser);
      logger.info(
        "Existing user '{}' updated to ensure {} access",
        existingUser.getUsername(),
        role
      );
    } else {
      logger.info(
        "{} user '{}' already present, no bootstrap change needed",
        role,
        existingUser.getUsername()
      );
    }
  }

  private void createBootstrapUser(
    String username,
    String rawPassword,
    String role
  ) {
    String email = buildBootstrapEmail(username, role);

    if (userRepository.existsByEmail(email)) {
      logger.warn(
        "Cannot create bootstrap {} '{}': generated email '{}' already exists",
        role,
        username,
        email
      );
      return;
    }

    User user = new User(
      username,
      email,
      passwordEncoder.encode(rawPassword)
    );
    user.setRole(role);

    userRepository.save(user);
    logger.info("Bootstrap {} user '{}' created", role, username);
  }

  private String buildBootstrapEmail(String username, String role) {
    return username.toLowerCase() + "+" + role.toLowerCase() + "@voctech.local";
  }
}
