package com.voctech.model;

import jakarta.persistence.*;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "themes")
@Getter
@Setter
@NoArgsConstructor
public class Theme {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "name_fr", nullable = false)
  private String nameFr;

  @Column(name = "name_en", nullable = false)
  private String nameEn;

  @Column(name = "desc_fr", columnDefinition = "TEXT", nullable = false)
  private String descFr;

  @Column(name = "desc_en", columnDefinition = "TEXT", nullable = false)
  private String descEn;

  @ManyToMany(mappedBy = "themes")
  private Set<Word> words;
}
