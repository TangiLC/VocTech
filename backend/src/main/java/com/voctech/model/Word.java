package com.voctech.model;

import jakarta.persistence.*;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "words")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Word {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String language;
  private String word;

  @ManyToMany
  @JoinTable(
    name = "themes_relations",
    joinColumns = @JoinColumn(name = "word_id"),
    inverseJoinColumns = @JoinColumn(name = "theme_id")
  )
  private Set<Theme> themes;

  @OneToMany(mappedBy = "wordSource")
  private Set<WordRelation> sourceRelations;
  
  @OneToMany(mappedBy = "wordTarget")
  private Set<WordRelation> targetRelations;
}
