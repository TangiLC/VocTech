package com.voctech.controller;

import com.voctech.payload.UpdateWordRequest;
import com.voctech.payload.WordResponse;
import com.voctech.service.WordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion des mots et leur association aux thèmes.
 */
@RestController
@RequestMapping("/voctech")
@Tag(
  name = "Voctech",
  description = "API pour la gestion du vocabulaire technique"
)
public class WordController {

  private final WordService wordService;

  /**
   * Constructeur du contrôleur WordController.
   *
   * @param wordService Service pour la gestion des mots
   */
  public WordController(WordService wordService) {
    this.wordService = wordService;
  }

  /**
   * Recherche des mots contenant une séquence de lettres donnée, sans distinction de casse et d'accentuation.
   *
   * @param request Objet contenant le mot recherché
   * @return Liste de mots correspondant au critère de recherche
   */
  @Operation(
    summary = "Recherche de mots dans les dictionnaires",
    description = "Recherche tous les mots contenant la séquence donnée, indépendamment des majuscules et des accents.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "Liste des mots trouvés",
        content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = WordResponse.class)
        )
      ),
      @ApiResponse(
        responseCode = "401",
        description = "Non autorisé (JWT invalide ou absent)"
      ),
    }
  )
  //@PreAuthorize("hasAuthority('USER','ADMIN')") 
  @GetMapping("/search")
  public ResponseEntity<List<WordResponse>> getWords(
    @RequestParam String word
  ) {
    List<WordResponse> words = wordService.searchWords(word);
    return ResponseEntity.ok(words);
  }


  /**
   * Récupère tous les mots du dictionnaire.
   *
   * @return Liste de tous les mots (WordResponse)
   */
  @Operation(
    summary = "Récupère tous les mots",
    description = "Renvoie la liste complète des mots du dictionnaire avec leurs relations (traductions, synonymes, etc.)"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Liste des mots récupérée avec succès"),
    @ApiResponse(responseCode = "401", description = "Non autorisé — authentification requise"),
    @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
  })
  @GetMapping("/words")
  public ResponseEntity<List<WordResponse>> getAllWords() {
    List<WordResponse> words = wordService.getAllWords();
    return ResponseEntity.ok(words);
  }


  /**
   * Met à jour un mot existant dans la base de données.
   *
   * @param request Objet contenant l'ID du mot et les nouvelles valeurs pour word, language et/ou themeId.
   * @return ResponseEntity avec un message de succès ou une erreur si l'ID est inexistant.
   */
  @Operation(
    summary = "Mise à jour d'un mot du dictionnaire",
    description = "Met à jour un mot existant en modifiant ses attributs word, language et/ou ses relations avec les thèmes.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "Mot mis à jour avec succès"
      ),
      @ApiResponse(
        responseCode = "400",
        description = "Erreur: ID inexistant ou données invalides",
        content = @Content(
          schema = @Schema(example = "{\"error\": \"ID du mot inexistant\"}")
        )
      ),
    }
  )
  @PreAuthorize("hasAuthority('ADMIN')")
  @PatchMapping("/")
  public ResponseEntity<?> updateWord(@RequestBody UpdateWordRequest request) {
    return wordService.updateWord(request);
  }

  @Operation(
    summary = "Suppression d'un mot",
    description = "Supprime un mot et ses relations en cascade.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "Mot supprimé avec succès"
      ),
      @ApiResponse(
        responseCode = "400",
        description = "Erreur: ID inexistant",
        content = @Content(
          schema = @Schema(example = "{\"error\": \"ID du mot inexistant\"}")
        )
      ),
    }
  )
  @PreAuthorize("hasAuthority('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteWord(@PathVariable Long id) {
    return wordService.deleteWord(id);
  }
}
