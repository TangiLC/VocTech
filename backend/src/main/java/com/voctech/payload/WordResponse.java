package com.voctech.payload;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WordResponse {

  private Long id;
  private String word;
  private String language;
  private List<Integer> themeId;
}
