package com.voctech.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
  description = "Requête pour ajouter une relation entre deux mots existants"
)
public class ExistingRelationRequest {

  @Schema(description = "Données du mot source existant", required = true)
  private WordEntry source;

  @Schema(description = "Données du mot cible existant", required = true)
  private WordEntry target;

  @Schema(
    description = "Type de relation entre les deux mots (translation, synonym, antonym)",
    required = true,
    example = "translation"
  )
  private String relation;

  @Getter
  @Setter
  public static class WordEntry {

    @Schema(description = "ID du mot existant", required = true, example = "1")
    private Integer id;
  }
}
