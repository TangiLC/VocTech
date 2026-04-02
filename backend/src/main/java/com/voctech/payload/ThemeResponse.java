package com.voctech.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour représenter un thème sans ses relations avec les mots.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThemeResponse {

  private Integer id;
  private String nameFr;
  private String nameEn;
  private String descFr;
  private String descEn;
}
