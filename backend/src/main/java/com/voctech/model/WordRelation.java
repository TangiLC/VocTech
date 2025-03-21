package com.voctech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
  name = "words_relations",
  uniqueConstraints = {
    @UniqueConstraint(
      columnNames = { "word_source_id", "word_target_id", "type" }
    ),
  }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WordRelation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "word_source_id", nullable = false)
  private Word wordSource;

  @ManyToOne
  @JoinColumn(name = "word_target_id", nullable = false)
  private Word wordTarget;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RelationType type;

  public enum RelationType {
    translation,
    synonym,
    antonym,
  }
}
