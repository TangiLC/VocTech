package com.voctech.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseRequest {

  @Schema(
    description = "Liste des deux entrées de mots à ajouter",
    required = true,
    example = "[{\"word\":\"épée\",\"language\":\"fr\",\"themeId\":[1,2]},{\"word\":\"sword\",\"language\":\"en\",\"themeId\":[1]}]"
  )
  private List<WordEntry> entries;

  @Schema(
    description = "Type de relation entre les deux mots (translation, synonym, antonym)",
    required = true,
    example = "translation"
  )
  private String relation;

  @Getter
  @Setter
  public static class WordEntry {

    @Schema(description = "Mot à ajouter", required = true, example = "épée")
    private String word;

    @Schema(description = "Langue du mot", required = true, example = "fr")
    private String language;

    @Schema(description = "Liste des ID des thèmes associés", example = "[1,2]")
    private List<Integer> themeId;
  }
}
