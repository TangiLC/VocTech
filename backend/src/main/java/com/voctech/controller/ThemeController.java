package com.voctech.controller;

import com.voctech.model.Theme;
import com.voctech.payload.ThemeResponse;
import com.voctech.service.ThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion des thèmes.
 */
@RestController
@RequestMapping("/voctech")
@Tag(
  name = "Voctech - Thèmes",
  description = "API pour la gestion des thèmes"
)
public class ThemeController {

  private final ThemeService themeService;

  /**
   * Constructeur du contrôleur ThemeController.
   *
   * @param themeService Service pour la gestion des thèmes
   */
  public ThemeController(ThemeService themeService) {
    this.themeService = themeService;
  }

  /**
   * Récupère l'ensemble des thèmes.
   *
   * @return Liste des thèmes sous forme de JSON
   */
  @Operation(
    summary = "Récupération de tous les thèmes",
    description = "Renvoie une liste contenant tous les thèmes avec leurs informations associées.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "Liste des thèmes trouvés",
        content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = Theme.class)
        )
      ),
      @ApiResponse(
        responseCode = "401",
        description = "Non autorisé (JWT invalide ou absent)"
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("/themes")
  public ResponseEntity<List<ThemeResponse>> getAllThemes() {
    List<ThemeResponse> themes = themeService.getAllThemes();
    return ResponseEntity.ok(themes);
  }
}
