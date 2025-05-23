package com.voctech.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
  name = "users",
  uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email"),
  }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(min = 3, max = 50)
  @Column(nullable = false, unique = true)
  private String username;

  @NotBlank
  @Size(min = 8, max = 100)
  @Column(nullable = false)
  private String password;

  @NotBlank
  @Email
  @Size(max = 100)
  @Column(nullable = false, unique = true)
  private String email;

  @NotBlank
  @Pattern(
    regexp = "^(USER|ADMIN|GUEST)$",
    message = "Role must be USER, ADMIN or GUEST"
  )
  @Column(nullable = false, length = 10)
  private String role;

  public User(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
