package com.voctech.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewRelationRequest {

  @Schema(
    description = "Données du mot source (doit exister en base)",
    required = true
  )
  private SourceEntry source;

  @Schema(description = "Données du nouveau mot cible", required = true)
  private TargetEntry target;

  @Schema(
    description = "Type de relation entre les deux mots (translation, synonym, antonym)",
    required = true,
    example = "translation"
  )
  private String relation;

  @Getter
  @Setter
  public static class SourceEntry {

    @Schema(
      description = "ID du mot source existant",
      required = true,
      example = "1"
    )
    private Integer id;
  }

  @Getter
  @Setter
  public static class TargetEntry {

    @Schema(description = "Mot à ajouter", required = true, example = "katana")
    private String word;

    @Schema(description = "Langue du mot", required = true, example = "ja")
    private String language;

    @Schema(description = "Liste des ID des thèmes associés", example = "[3,5]")
    private List<Integer> themeId;
  }
}
