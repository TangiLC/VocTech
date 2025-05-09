package com.voctech.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
  @EqualsAndHashCode.Include
  private Word wordSource;

  @ManyToOne
  @JoinColumn(name = "word_target_id", nullable = false)
  @EqualsAndHashCode.Include
  private Word wordTarget;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @EqualsAndHashCode.Include
  private RelationType type;

  public enum RelationType {
    translation,
    synonym,
    antonym,
  }
}
