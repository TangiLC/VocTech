package com.voctech.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RelatedWordResponse {

  private Long id;
  private String word;
  private String language;
}
