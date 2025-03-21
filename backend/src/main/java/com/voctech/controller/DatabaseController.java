package com.voctech.controller;

import com.voctech.payload.DatabaseRequest;
import com.voctech.payload.ExistingRelationRequest;
import com.voctech.payload.NewRelationRequest;
import com.voctech.service.DatabaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/database")
@Tag(
  name = "Database",
  description = "API pour la gestion de la base de données du dictionnaire"
)
public class DatabaseController {

  private final DatabaseService databaseService;

  public DatabaseController(DatabaseService databaseService) {
    this.databaseService = databaseService;
  }

  @Operation(
    summary = "Ajout d'une paire de mots et de relations",
    description = "Ajoute deux nouveaux mots avec leurs thèmes et crée une relation entre eux.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "Les mots ont été ajoutés avec succès"
      ),
      @ApiResponse(
        responseCode = "400",
        description = "Erreur: Un mot existe déjà ou un champ est vide",
        content = @Content(
          schema = @Schema(
            example = "{\"error\": \"Mot déjà existant ou champ vide\"}"
          )
        )
      ),
    }
  )
  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping("/addpair")
  public ResponseEntity<?> addWordsWithRelations(
    @RequestBody DatabaseRequest request
  ) {
    return databaseService.addWordsWithRelations(request);
  }

  @Operation(
    summary = "Ajout d'un nouveau mot et d'une relation avec un mot existant",
    description = "Ajoute un nouveau mot et crée une relation avec un mot existant.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "Le mot et la relation ont été ajoutés avec succès"
      ),
      @ApiResponse(
        responseCode = "400",
        description = "Erreur: Le mot existe déjà, l'ID source est invalide ou un champ est vide",
        content = @Content(
          schema = @Schema(
            example = "{\"error\": \"Mot déjà existant ou ID source invalide\"}"
          )
        )
      ),
    }
  )
  @PreAuthorize("hasAuthority('ADMIN')")
  @PatchMapping("/addword")
  public ResponseEntity<?> addNewRelation(
    @RequestBody NewRelationRequest request
  ) {
    return databaseService.addNewRelation(request);
  }

  @Operation(
    summary = "Ajout d'une relation entre deux mots existants",
    description = "Crée une relation entre deux mots déjà présents en base.",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Données pour créer une relation entre deux mots existants",
      required = true,
      content = @Content(
        schema = @Schema(
          example = "{ \"source\": { \"id\": 1 }, \"target\": { \"id\": 2 }, \"relation\": \"translation\" }"
        )
      )
    ),
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "La relation a été ajoutée avec succès"
      ),
      @ApiResponse(
        responseCode = "400",
        description = "Erreur: L'un des mots n'existe pas ou un champ est vide",
        content = @Content(
          schema = @Schema(
            example = "{\"error\": \"ID du mot source ou cible invalide\"}"
          )
        )
      ),
    }
  )
  @PreAuthorize("hasAuthority('ADMIN')")
  @PatchMapping("/addrelation")
  public ResponseEntity<?> addExistingRelation(
    @RequestBody ExistingRelationRequest request
  ) {
    return databaseService.addExistingRelation(request);
  }
}
