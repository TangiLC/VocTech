package com.voctech.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateWordRequest {

  @Schema(
    description = "ID du mot à mettre à jour",
    required = true,
    example = "1"
  )
  private Integer id;

  @Schema(description = "Nouveau mot (optionnel)", example = "épée")
  private String word;

  @Schema(description = "Nouvelle langue (optionnel)", example = "fr")
  private String language;

  @Schema(
    description = "Nouvelle liste d'ID des thèmes associés (optionnel)",
    example = "[1,2]"
  )
  private List<Integer> themeId;
}
