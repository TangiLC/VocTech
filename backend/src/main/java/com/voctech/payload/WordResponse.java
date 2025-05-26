package com.voctech.payload;

import java.util.List;
import java.util.Map;
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
  private Map<String, List<RelatedWordResponse>> relations;
}
