package com.voctech.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

  @NotBlank
  @Size(
    min = 3,
    max = 20,
    message = "Le nom doit avoir une taille entre 3 et 20 caractères"
  )
  @Pattern(
    regexp = "^[a-zA-Z0-9._-]+$",
    message = "Les caractères autorisés sont lettres, chiffres et . _ ou - uniquement"
  )
  private String username;

  @NotBlank
  @Size(max = 50)
  @Email
  @Pattern(
    regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
    message = "L'adresse email doit être au format standard (nom@domaine.ext)"
  )
  private String email;

  private Set<String> roles;

  @NotBlank
  @Size(
    min = 6,
    max = 40,
    message = "Le mot de passe doit avoir une taille entre 6 et 40 caractères"
  )
  @Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\+\\-\\*/!:<>§%$#]).{6,40}$",
    message = "Le mot de passe doit contenir au moins 1 majuscule, 1 minuscule, 1 chiffre et 1 caractère spécial"
  )
  private String password;
}
